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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

    private final ConcurrentHashMap<String, Future<?>> runningTasks = new ConcurrentHashMap<>();

    private final ThreadPoolExecutor downloadExecutor = new ThreadPoolExecutor(4, 8,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    @Override
    public Result<String> submit(String url) throws IOException, URISyntaxException {
        String taskId = UUIDUtils.generateId();
        Task task = initOneTask(taskId, url);
        return null;
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
        String outputPath = downloadPath + "/" + fileName;

        Task task = new Task(
                id,
                fileName,
                conn.getContentType(),
                fileSize,
                urlString,
                outputPath,
                Constants.TASK_STATUS_PENDING,
                Constants.DEFAULT_THREADS,
                chunkNums,
                chunkSize
        );

        taskMapper.insert(task);
        return task;
    }

    private HttpURLConnection getConn(String urlStr) throws IOException, URISyntaxException {
        URI uri = new URI(urlStr);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
        conn.setRequestProperty("Accept", "*/*");

        int respCode = conn.getResponseCode();
        if (respCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to fetch file: HTTP response code " + respCode);
        }
        return conn;
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
        return null;
    }

    @Override
    public Result<List<String>> delete(List<String> ids) {
        return null;
    }

    @Override
    public Result<List<Task>> getTaskList(String status) {
        return null;
    }
}
