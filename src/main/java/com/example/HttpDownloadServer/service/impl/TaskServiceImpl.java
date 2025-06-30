package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.common.BlockingThreadPoolExecutor;
import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.dao.SettingsMapper;
import com.example.HttpDownloadServer.dao.TaskMapper;
import com.example.HttpDownloadServer.service.RedisService;
import com.example.HttpDownloadServer.service.SseService;
import com.example.HttpDownloadServer.service.TaskService;
import com.example.HttpDownloadServer.param.Result;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@SuppressWarnings("UnstableApiUsage")
public class TaskServiceImpl implements TaskService {
    @Autowired
    private SettingsMapper settingsMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SseService sseService;

    private static final Object lock = new Object();

    private final Map<String, List<Future<?>>> chunkFutures = new ConcurrentHashMap<>();
    
    // Task progress tracking with atomic operations
    private final Map<String, AtomicLong> taskProgressMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> taskStartTimeMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> taskLastUpdateTimeMap = new ConcurrentHashMap<>();
    
    // Scheduled executor for progress updates
    private final ScheduledExecutorService progressExecutor = Executors.newSingleThreadScheduledExecutor(
        new ThreadFactoryBuilder().setNameFormat("progress-updater-%d").build()
    );

    private ExecutorService downloadExecutor;

    @PostConstruct
    public void init() {
        int maximumPoolSize = Math.max(2, Runtime.getRuntime().availableProcessors());
        downloadExecutor = new BlockingThreadPoolExecutor(maximumPoolSize * 2, "download-server-executor");
    }

    @PreDestroy
    public void destroy() {
        downloadExecutor.shutdown();
        progressExecutor.shutdown();
        try {
            if (!downloadExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                downloadExecutor.shutdownNow();
            }
            if (!progressExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                progressExecutor.shutdownNow();
            }
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public Result<String> submit(String url) {
        Result<String> result = new Result<>();
        String taskId = UUID.randomUUID().toString();
        result.setCode(Constants.HTTP_STATUS_OK);
        result.setData(taskId);

        // Asynchronous processing download
        CompletableFuture.runAsync(() -> {
            try {
                Task task = initOneTask(taskId, url);
                redisService.initializeScoreboard(taskId, task.getChunkNum());
                processDownload(task);
            } catch (IOException | URISyntaxException e) {
                log.error("Submit task error id:{} err:{}", taskId, e.getMessage(), e);
                result.setCode(Constants.HTTP_STATUS_SERVER_ERROR);
                result.setMessage(Constants.ERR_SUBMIT_TASK);
            }
        });
        return result;
    }

    private void processDownload(Task task) throws IOException, URISyntaxException {
        task.setStatus(Constants.TASK_STATUS_DOWNLOADING);
        taskMapper.updateById(task);
        
        // Start progress tracking
        startProgressTracking(task);
        
        File outputFile = new File(task.getSavePath());
        List<Future<?>> futures = new ArrayList<>();
        List<Integer> scoreboard = redisService.getScoreboard(task.getId());
        RateLimiter limiter = RateLimiter.create(settingsMapper.selectOne(null).getMaxDownloadSpeed() * 1000 * 1000);

        // Submit the fragment for download
        for (int i = 0; i < task.getChunkNum(); i++) {
            int start = i * task.getChunkSize();
            int end = (int) Math.min(task.getSize(), start + task.getChunkSize()) - 1;
            int chunkIndex = i;
            if (scoreboard.contains(chunkIndex)) {
                Future<?> future = downloadExecutor.submit(() -> downloadChunk(task, start, end, outputFile, chunkIndex, limiter));
                futures.add(future);
            }
        }
        chunkFutures.put(task.getId(), futures);
    }

    private void downloadChunk(Task task, int start, int end, File file, int index, RateLimiter limiter) {
        try {
            HttpURLConnection conn = getConn(task.getUrl());
            conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            RandomAccessFile raf = new RandomAccessFile(file, "rw");

            // Seek to start position
            raf.seek(start);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                // Check whether the current thread is interrupted
                if (Thread.currentThread().isInterrupted()) {
                    log.info("Download paused for task id:{} threadId:{}", task.getId(), Thread.currentThread().threadId());
                    break;
                }
                
                limiter.acquire(bytesRead);
                raf.write(buffer, 0, bytesRead);
                
                // Update progress atomically
                updateTaskProgress(task.getId(), bytesRead);
            }

            // Mark chunk as completed
            markChunkCompleted(task.getId(), index);

            in.close();
            raf.close();
            conn.disconnect();
        } catch (IOException e) {
            log.error("Download failed id:{} err:{}", task.getId(), e.getMessage());
            handleTaskFailure(task);
        }
    }

    @Override
    public Result<List<String>> pause(List<String> ids) {
        Result<List<String>> result = new Result<>();
        List<Task> tasks = taskMapper.selectBatchIds(ids);
        for (Task task : tasks) {
            if (task.getStatus().equals(Constants.TASK_STATUS_DOWNLOADING)) {
                task.setStatus(Constants.TASK_STATUS_CANCELED);
                taskMapper.updateById(task);

                List<Future<?>> futures = chunkFutures.get(task.getId());
                if (futures != null) {
                    for (Future<?> future : futures) {
                        future.cancel(true);
                    }
                } else {
                    log.error("The task futures is null id:{}", task.getId());
                }
                sseService.close(task.getId());
                cleanupTaskResources(task.getId());
            } else {
                log.error("The task status is not downloading id:{} status:{}", task.getId(), task.getStatus());
                result.setCode(Constants.HTTP_STATUS_BAD_REQUEST);
                result.setMessage("Task status is not downloading");
                return result;
            }
        }
        result.setData(ids);
        result.setCode(Constants.HTTP_STATUS_OK);
        return result;
    }

    @Override
    public Result<List<String>> resume(List<String> ids) {
        Result<List<String>> result = new Result<>();
        List<Task> tasks = taskMapper.selectBatchIds(ids);
        for (Task task : tasks) {
            if (task.getStatus().equals(Constants.TASK_STATUS_CANCELED)) {
                task.setStatus(Constants.TASK_STATUS_PENDING);
                taskMapper.updateById(task);

                CompletableFuture.runAsync(() -> {
                    try {
                        processDownload(task);
                    } catch (IOException | URISyntaxException e) {
                        log.error("Submit task error id:{} err:{}", task.getId(), e.getMessage(), e);
                        result.setCode(Constants.HTTP_STATUS_SERVER_ERROR);
                        result.setMessage("Resume task failed id:{}" + task.getId());
                    }
                });
            } else {
                log.error("The task status is not canceled id:{} status:{}", task.getId(), task.getStatus());
                result.setCode(Constants.HTTP_STATUS_BAD_REQUEST);
                result.setMessage("Resume task failed id:{}" + task.getId());
                return result;
            }
        }
        return result;
    }

    @Override
    public Result<List<String>> restart(List<String> ids) {
        Result<List<String>> result = new Result<>();
        List<Task> tasks = taskMapper.selectBatchIds(ids);
        for (Task task : tasks) {
            if (task.getStatus().equals(Constants.TASK_STATUS_DOWNLOADED)) {
                task.setStatus(Constants.TASK_STATUS_PENDING);
                task.setTotalDownloaded(0);
                task.setProgress(0);
                task.setSpeed(0);
                taskMapper.updateById(task);
                redisService.initializeScoreboard(task.getId(), task.getChunkNum());

                CompletableFuture.runAsync(() -> {
                    try {
                        processDownload(task);
                    } catch (IOException | URISyntaxException e) {
                        log.error("Submit task error id:{} err:{}", task.getId(), e.getMessage(), e);
                        result.setCode(Constants.HTTP_STATUS_SERVER_ERROR);
                        result.setMessage("Restart task failed id:{}" + task.getId());
                    }
                });
            } else {
                log.error("The task status is not downloaded id:{} status:{}", task.getId(), task.getStatus());
                result.setCode(Constants.HTTP_STATUS_SERVER_ERROR);
                result.setMessage("Restart task failed id:{}" + task.getId());
                return result;
            }
        }
        result.setData(ids);
        result.setCode(Constants.HTTP_STATUS_OK);
        return result;
    }

    @Override
    public Result<List<String>> delete(List<String> ids) {
        Result<List<String>> result = new Result<>();
        if (ids.isEmpty()) {
            result.setCode(Constants.HTTP_STATUS_BAD_REQUEST);
            result.setMessage("task is null");
            return result;
        }
        taskMapper.deleteByIds(ids);
        for (String id : ids) {
            redisService.deleteScoreboard(id);
            cleanupTaskResources(id);
        }
        result.setData(ids);
        result.setCode(Constants.HTTP_STATUS_OK);
        return result;
    }

    @Override
    public Result<List<Task>> getTaskList(String status) {
        Result<List<Task>> result = new Result<>();
        if (status.equals(Constants.Task_Status_ALL)) {
            result.setData(taskMapper.selectList(null));
        } else {
            result.setData(taskMapper.getTasksByStatus(status));
        }
        result.setCode(Constants.HTTP_STATUS_OK);
        return result;
    }

    private int getChunkSize(long fileSize) {
        int chunkSize;
        if (fileSize < 10 * 1024 * 1024) {
            chunkSize = Constants.MIN_CHUNK_SIZE;
        } else if (fileSize < 100 * 1024 * 1024) {
            chunkSize = Constants.MID_CHUNK_SIZE;
        } else {
            chunkSize = Constants.MAX_CHUNK_SIZE;
        }
        return chunkSize;
    }

    private Task initOneTask(String id, String urlString) throws IOException, URISyntaxException {
        String downloadPath = settingsMapper.selectOne(null).getDownloadPath();

        HttpURLConnection conn = getConn(urlString);

        long fileSize = conn.getContentLength();
        int chunkSize = getChunkSize(fileSize);
        int chunkNums = (int) ((fileSize + chunkSize - 1) / chunkSize);

        String fileName = extractFileName(conn, urlString);
        String ext = fileName.substring(fileName.lastIndexOf("."));
        String outputPath = downloadPath + "/" + fileName;

        log.info("Init a task, id:{} fileSize:{} savePath:{} chunkSize:{} chunkNums:{}", id, fileSize, outputPath, chunkSize, chunkNums);

        Task task = new Task(
                id,
                fileName,
                ext,
                fileSize,
                urlString,
                outputPath,
                Constants.TASK_STATUS_PENDING,
                Constants.DEFAULT_THREADS,
                chunkNums,
                chunkSize,
                LocalDateTime.now()
        );

        taskMapper.insert(task);
        return task;
    }

    private HttpURLConnection getConn(String urlStr) {
        try {
            URI uri = new URI(urlStr);
            URL url = uri.toURL();
            return (HttpURLConnection) url.openConnection();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractFileName(HttpURLConnection connection, String urlString) {
        String fileName = null;

        Map<String, List<String>> headers = connection.getHeaderFields();
        List<String> contentDisposition = headers.get("Content-Disposition");

        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            String disposition = contentDisposition.getFirst();
            if (disposition.contains("filename=")) {
                fileName = disposition.substring(disposition.indexOf("filename=") + 9);
                if (fileName.startsWith("\"") && fileName.endsWith("\"")) {
                    fileName = fileName.substring(1, fileName.length() - 1);
                }
            }
        }

        if (fileName == null || fileName.isEmpty()) {
            try {
                URI uri = new URI(urlString);
                String path = uri.getPath();
                fileName = path.substring(path.lastIndexOf("/") + 1);
            } catch (URISyntaxException e) {
                fileName = "unknown";
            }
        }

        return fileName;
    }

    // Progress tracking methods
    private void startProgressTracking(Task task) {
        String taskId = task.getId();
        long startTime = System.currentTimeMillis();
        
        taskStartTimeMap.put(taskId, new AtomicLong(startTime));
        taskLastUpdateTimeMap.put(taskId, new AtomicLong(startTime));
        
        // Schedule periodic progress updates
        progressExecutor.scheduleAtFixedRate(() -> {
            try {
                updateTaskProgressAndNotify(taskId);
            } catch (Exception e) {
                log.error("Error updating progress for task: {}", taskId, e);
            }
        }, Constants.MessageInterval, Constants.MessageInterval, TimeUnit.MILLISECONDS);
    }

    private void updateTaskProgress(String taskId, int bytesRead) {
        AtomicLong progress = taskProgressMap.computeIfAbsent(taskId, k -> new AtomicLong(0));
        progress.addAndGet(bytesRead);
    }

    private void markChunkCompleted(String taskId, int chunkIndex) {
        synchronized (lock) {
            redisService.updateScoreboard(taskId, chunkIndex);

            // Check if all chunks are completed
            if (redisService.getScoreboard(taskId).isEmpty()) {
                log.info("Download complete id:{}", taskId);
                handleTaskFinish(taskId);
            }
        }
    }

    private void handleTaskFailure(Task task) {
        task.setStatus(Constants.TASK_STATUS_FAILED);
        taskMapper.updateById(task);
        cleanupTaskResources(task.getId());
    }

    private void handleTaskFinish(String taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task != null) {
            redisService.deleteScoreboard(taskId);
            task.setProgress(100);
            task.setRemainingTime(0);
            task.setStatus(Constants.TASK_STATUS_DOWNLOADED);
            taskMapper.updateById(task);
            sseService.send(taskId, task);
            sseService.close(taskId);
            cleanupTaskResources(taskId);
        }
    }

    private void cleanupTaskResources(String taskId) {
        taskProgressMap.remove(taskId);
        taskStartTimeMap.remove(taskId);
        taskLastUpdateTimeMap.remove(taskId);
        chunkFutures.remove(taskId);
    }

    private void updateTaskProgressAndNotify(String taskId) {
        AtomicLong progress = taskProgressMap.get(taskId);
        AtomicLong startTime = taskStartTimeMap.get(taskId);
        AtomicLong lastUpdateTime = taskLastUpdateTimeMap.get(taskId);
        
        if (progress == null || startTime == null || lastUpdateTime == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long lastUpdate = lastUpdateTime.get();
        
        // Only update if enough time has passed
        if (currentTime - lastUpdate < Constants.MessageInterval) {
            return;
        }

        Task task = taskMapper.selectById(taskId);
        if (task == null || !task.getStatus().equals(Constants.TASK_STATUS_DOWNLOADING)) {
            return;
        }

        long totalDownloaded = progress.get();
        long elapsedTime = currentTime - startTime.get();
        
        // Calculate metrics
        double speed = calculateSpeed(totalDownloaded, elapsedTime);
        double progressPercent = calculateProgress(totalDownloaded, task.getSize());
        double remainingTime = calculateRemainingTime(totalDownloaded, task.getSize(), speed);

        // Update task with new metrics
        task.setTotalDownloaded(totalDownloaded);
        task.setSpeed(speed);
        task.setProgress(progressPercent);
        task.setRemainingTime(remainingTime);

        // Send SSE update
        sseService.send(taskId, task);
        
        // Update database (less frequently to reduce DB load)
        if (currentTime - lastUpdate >= Constants.MessageInterval * 2) {
            taskMapper.updateById(task);
            lastUpdateTime.set(currentTime);
        }
    }

    private double calculateSpeed(long bytesDownloaded, long elapsedTimeMs) {
        if (elapsedTimeMs <= 0) return 0.0;
        double speedMBps = (bytesDownloaded / (elapsedTimeMs / 1000.0)) / (1024 * 1024);
        return Math.round(speedMBps * 100.0) / 100.0;
    }

    private double calculateProgress(long bytesDownloaded, long totalSize) {
        if (totalSize <= 0) return 0.0;
        double progress = (bytesDownloaded * 100.0) / totalSize;
        return Math.round(progress * 100.0) / 100.0;
    }

    private double calculateRemainingTime(long bytesDownloaded, long totalSize, double speedMBps) {
        if (speedMBps <= 0) return 0.0;
        long remainingBytes = totalSize - bytesDownloaded;
        double remainingTimeSeconds = (remainingBytes / (1024.0 * 1024.0)) / speedMBps;
        return Math.round(remainingTimeSeconds * 100.0) / 100.0;
    }
}
