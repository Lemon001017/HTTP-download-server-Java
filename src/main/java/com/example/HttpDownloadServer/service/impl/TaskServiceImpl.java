package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.mapper.SettingsMapper;
import com.example.HttpDownloadServer.mapper.TaskMapper;
import com.example.HttpDownloadServer.service.TaskService;
import com.example.HttpDownloadServer.utils.Result;
import com.example.HttpDownloadServer.utils.UUIDUtils;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private SettingsMapper settingsMapper;

    @Autowired
    private TaskMapper taskMapper;

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private static final Object lock = new Object();

    private final ConcurrentHashMap<String, List<Future<?>>> runningTasks = new ConcurrentHashMap<>();

    private final ThreadPoolExecutor downloadExecutor = new ThreadPoolExecutor(4, 8,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));

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
        File outputFile = new File(task.getSavePath());

        // Submit the fragment for download
        for (int i = 0; i < task.getChunkNum(); i++) {
            int start = i * task.getChunkSize();
            int end = (int) Math.min(task.getSize(), start + task.getChunkSize()) - 1;
            downloadExecutor.submit(() -> downloadChunk(task, start, end, outputFile));
        }
    }

    private void downloadChunk(Task task, int start, int end, File file) {
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

            while ((bytesRead = in.read(buffer)) != -1) {
                raf.write(buffer, 0, bytesRead);
                synchronized (lock) {
                    task.setTotalDownloaded(task.getTotalDownloaded() + bytesRead);
                    taskMapper.updateById(task);
                }
            }

            if (task.getTotalDownloaded() == task.getSize()) {
                log.info("Download complete id:{} url:{}", task.getId(), task.getUrl());
                task.setStatus(Constants.TASK_STATUS_DOWNLOADED);
                taskMapper.updateById(task);
            }

            in.close();
            raf.close();
            conn.disconnect();
        } catch (IOException | URISyntaxException e) {
            log.error("Download chunk error id:{} err:{}", task.getId(), e.getMessage());
            task.setStatus(Constants.TASK_STATUS_FAILED);
            taskMapper.updateById(task);
        }
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

        long fileSize = conn.getContentLengthLong();
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

    @Override
    public Result<List<String>> pause(List<String> ids) {
        return null;
    }

    @Override
    public Result<List<String>> resume(List<String> ids) {
        return null;
    }

    @Override
    public Result<List<String>> restart(List<String> ids) {
        Result<List<String>> result = new Result<>();
        result.setData(ids);
        result.setCode(Constants.HTTP_STATUS_OK);

        List<Task> tasks = taskMapper.selectBatchIds(ids);

        for (Task task : tasks) {
            if (task.getStatus().equals(Constants.TASK_STATUS_DOWNLOADED)) {
                task.setStatus(Constants.TASK_STATUS_PENDING);
                task.setTotalDownloaded(0);
                task.setProgress(0);
                task.setSpeed(0);
                taskMapper.updateById(task);

                CompletableFuture.runAsync(() -> {
                    try {
                        processDownload(task);
                    } catch (IOException | URISyntaxException e) {
                        log.error("Submit task error id:{} err:{}", task.getId(), e.getMessage(), e);
                        result.setCode(Constants.HTTP_STATUS_SERVER_ERROR);
                        result.setMessage(Constants.ERR_SUBMIT_TASK);
                    }
                });
            } else {
                log.error("The task status is not downloaded id:{} status:{}", task.getId(), task.getStatus());
                result.setCode(Constants.HTTP_STATUS_SERVER_ERROR);
                result.setMessage(Constants.ERR_SUBMIT_TASK);
            }
        }

        return result;
    }

    @Override
    public Result<List<String>> delete(List<String> ids) {
        Result<List<String>> result = new Result<>();
        taskMapper.deleteByIds(ids);
        result.setData(ids);
        result.setCode(Constants.HTTP_STATUS_OK);
        return result;
    }

    @Override
    public Result<List<Task>> getTaskList(String status) {
        Result<List<Task>> result = new Result<>();
        result.setData(taskMapper.getTasksByStatus(status));
        result.setCode(Constants.HTTP_STATUS_OK);
        return result;
    }
}
