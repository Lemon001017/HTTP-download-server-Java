package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.mapper.SettingsMapper;
import com.example.HttpDownloadServer.mapper.TaskMapper;
import com.example.HttpDownloadServer.service.RedisService;
import com.example.HttpDownloadServer.service.SseService;
import com.example.HttpDownloadServer.service.TaskService;
import com.example.HttpDownloadServer.utils.Result;
import com.example.HttpDownloadServer.utils.UUIDUtils;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.*;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private SettingsMapper settingsMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SseService sseService;

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private static final Object lock = new Object();

    private final ConcurrentHashMap<String, List<Future<?>>> chunkFutures = new ConcurrentHashMap<>();

    private final ThreadPoolExecutor downloadExecutor = new ThreadPoolExecutor(4, 8,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    @Override
    public Result<String> submit(String url) {
        Result<String> result = new Result<>();
        String taskId = UUIDUtils.generateId();
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

    @SuppressWarnings("UnstableApiUsage")
    private void processDownload(Task task) throws IOException, URISyntaxException {
        long startTime = System.currentTimeMillis();
        task.setStatus(Constants.TASK_STATUS_DOWNLOADING);
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
                Future<?> future = downloadExecutor.submit(() -> downloadChunk(task, start, end, outputFile, startTime, chunkIndex, limiter));
                futures.add(future);
            }
        }
        chunkFutures.put(task.getId(), futures);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void downloadChunk(Task task, int start, int end, File file, long startTime, int index, RateLimiter limiter) {
        try {
            HttpURLConnection conn = getConn(task.getUrl());
            conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            RandomAccessFile raf = new RandomAccessFile(file, "rw");

            synchronized (lock) {
                raf.seek(start);
            }

            byte[] buffer = new byte[4096];
            int bytesRead;

            long lastMessageTime = System.currentTimeMillis();

            while ((bytesRead = in.read(buffer)) != -1) {
                // Check whether the current thread is interrupted
                if (Thread.currentThread().isInterrupted()) {
                    log.info("Download paused for task id:{} threadId:{}", task.getId(), Thread.currentThread().threadId());
                    in.close();
                    raf.close();
                    conn.disconnect();
                    return;
                }
                limiter.acquire(bytesRead);
                raf.write(buffer, 0, bytesRead);
                synchronized (lock) {
                    task.setTotalDownloaded(task.getTotalDownloaded() + bytesRead);
                    if (System.currentTimeMillis() - lastMessageTime >= Constants.MessageInterval) {
                        // Calculate download data
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        double speed = Math.round((task.getTotalDownloaded() / (elapsedTime / 1000.0) / 1024 / 1024) * 100.0) / 100.0;
                        double progress = Math.round((task.getTotalDownloaded() * 1.0 * 100 / task.getSize()) * 100.0) / 100.0;
                        double remainingTime = Math.round((((task.getSize() - task.getTotalDownloaded()) / 1024.0 / 1024.0) / speed) * 100.0) / 100.0;

                        task.setSpeed(speed);
                        task.setProgress(progress);
                        task.setRemainingTime(remainingTime);

                        sseService.send(task.getId(), task);
                        taskMapper.updateById(task);
                        lastMessageTime = System.currentTimeMillis();
                    }
                }
            }

            synchronized (lock) {
                redisService.updateScoreboard(task.getId(), index);
            }

            if (task.getTotalDownloaded() == task.getSize() || redisService.getScoreboard(task.getId()).isEmpty()) {
                log.info("Download complete id:{} url:{}", task.getId(), task.getUrl());
                redisService.deleteScoreboard(task.getId());
                task.setProgress(100);
                task.setRemainingTime(0);
                task.setStatus(Constants.TASK_STATUS_DOWNLOADED);
                taskMapper.updateById(task);
                sseService.send(task.getId(), task);
            }

            in.close();
            raf.close();
            conn.disconnect();
        } catch (IOException | URISyntaxException e) {
            log.error("Download failed id:{} err:{}", task.getId(), e.getMessage());
            task.setStatus(Constants.TASK_STATUS_FAILED);
            taskMapper.updateById(task);
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
        taskMapper.deleteByIds(ids);
        for (String id : ids) {
            redisService.deleteScoreboard(id);
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

    private HttpURLConnection getConn(String urlStr) throws IOException, URISyntaxException {
        URI uri = new URI(urlStr);
        URL url = uri.toURL();
        return (HttpURLConnection) url.openConnection();
    }

    private String extractFileName(HttpURLConnection connection, String urlString) {
        String fileName = null;

        Map<String, List<String>> headers = connection.getHeaderFields();
        List<String> contentDisposition = headers.get("Content-Disposition");

        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            String disposition = contentDisposition.getFirst();
            int index = disposition.indexOf("filename=");
            if (index > 0) {
                fileName = disposition.substring(index + 10, disposition.length() - 1);
            }
        }

        if (fileName == null) {
            fileName = urlString.substring(urlString.lastIndexOf("/") + 1);
        }

        return fileName;
    }
}
