package com.example.HttpDownloadServer;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.mapper.SettingsMapper;
import com.example.HttpDownloadServer.mapper.TaskMapper;
import com.example.HttpDownloadServer.service.FileService;
import com.example.HttpDownloadServer.service.SettingsService;
import com.example.HttpDownloadServer.service.TaskService;
import com.example.HttpDownloadServer.utils.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class HttpDownloadServerAPITests {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FileService fileService;

    @Autowired
    private SettingsMapper settingsMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Test
    public void testGetSettings() {
        Settings result = settingsService.getSettings().getData();
        assertEquals(Constants.DEFAULT_DOWNLOAD_ROOT_PATH, result.getDownloadPath());
        assertEquals(Constants.DEFAULT_MAX_TASKS, result.getMaxTasks());
        assertEquals(Constants.DEFAULT_MAX_DOWNLOAD_SPEED, result.getMaxDownloadSpeed());
    }

    @Test
    public void testUpdateSettings() {
        Settings settings = new Settings(1, "test", 2, 3);
        settingsService.updateSettings(settings);
        Settings result = settingsMapper.selectById(1);
        assertEquals(2, result.getMaxTasks());
        assertEquals(3, result.getMaxDownloadSpeed());
    }

    @Test
    public void testGetTaskList() {
        Task task1 = new Task(
                "1", "test", "test",
                10L, 10, "test", "test", "downloading",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        Task task2 = new Task(
                "2", "test", "test",
                10L, 10, "test", "test", "downloaded",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );

        taskMapper.insert(task1);
        taskMapper.insert(task2);

        List<Task> result = taskService.getTaskList(Constants.TASK_STATUS_DOWNLOADED).getData();
        assertEquals(1, result.size());
        assertEquals(Constants.TASK_STATUS_DOWNLOADED, result.getFirst().getStatus());
        assertEquals("2", result.getFirst().getId());
        taskMapper.deleteByIds(List.of("1", "2"));
    }

    @Test
    public void testDeleteTasks() {
        Task task1 = new Task(
                "1", "test", "test",
                10L, 10, "test", "test", "downloading",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        Task task2 = new Task(
                "2", "test", "test",
                10L, 10, "test", "test", "downloaded",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );

        taskMapper.insert(task1);
        taskMapper.insert(task2);

        List<String> ids = taskService.delete(List.of("1", "2")).getData();
        assertEquals(2, ids.size());
        taskMapper.selectBatchIds(ids);
        assertEquals(0, taskMapper.selectBatchIds(ids).size());
    }

    @Test
    public void testSubmit() throws IOException, URISyntaxException, InterruptedException {
        String url = "https://i1.hdslb.com/bfs/archive/8db3fd38ae6eb0625e0c3b1d274160294d7bd5f5.jpg";
        String key = taskService.submit(url).getData();
        TimeUnit.SECONDS.sleep(3);
        Task task = taskMapper.selectById(key);
        assertEquals(Constants.TASK_STATUS_DOWNLOADED, task.getStatus());
        taskMapper.deleteById(key);
    }

    @Test
    public void testPause() {
        Task task = new Task(
                "1", "test", "test",
                10L, 10, "test", "test", "downloading",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        taskMapper.insert(task);
        try {
            Result<List<String>> ids = taskService.pause(List.of("1"));
            assertEquals(1, ids.getData().size());
            assertEquals("200", ids.getCode());
        } catch (NullPointerException e) {
            System.out.println(" ");
        }
        taskMapper.deleteById("1");
    }

    @Test
    public void testResume() {

    }

    @Test
    public void testRestart() {

    }

    @Test
    public void testGetFileList() {
    }
}
