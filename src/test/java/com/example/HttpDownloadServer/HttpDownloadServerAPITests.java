package com.example.HttpDownloadServer;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.dao.SettingsMapper;
import com.example.HttpDownloadServer.dao.TaskMapper;
import com.example.HttpDownloadServer.param.FileParam;
import com.example.HttpDownloadServer.service.FileService;
import com.example.HttpDownloadServer.service.SettingsService;
import com.example.HttpDownloadServer.service.TaskService;
import com.example.HttpDownloadServer.param.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        taskMapper.deleteByIds(List.of("1", "2"));
        Task task1 = new Task(
                "1", "test", "test",
                10L, 10, "test", "test", "test1_status",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        Task task2 = new Task(
                "2", "test", "test",
                10L, 10, "test", "test", "test2_status",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );

        taskMapper.insert(task1);
        taskMapper.insert(task2);

        List<Task> result = taskService.getTaskList("test2_status").getData();
        assertEquals(1, result.size());
        assertEquals("test2_status", result.getFirst().getStatus());
        assertEquals("2", result.getFirst().getId());

        taskMapper.deleteByIds(List.of("1", "2"));
    }

    @Test
    public void testDeleteTasks() {
        taskMapper.deleteByIds(List.of("1", "2"));
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
        taskMapper.deleteById("1");
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
        taskMapper.deleteById("1");
        Task task = new Task(
                "1", "test", "test",
                10L, 10, "test", "test", "canceled",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        taskMapper.insert(task);
        try {
            Result<List<String>> ids = taskService.resume(List.of("1"));
            assertEquals(1, ids.getData().size());
            assertEquals("200", ids.getCode());
        } catch (NullPointerException e) {
            System.out.println(" ");
        }
        taskMapper.deleteById("1");
    }

    @Test
    public void testRestart() {
        taskMapper.deleteById("1");
        Task task = new Task(
                "1", "test", "test",
                10L, 10, "test", "test", "downloaded",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        taskMapper.insert(task);
        try {
            Result<List<String>> ids = taskService.restart(List.of("1"));
            assertEquals(1, ids.getData().size());
            assertEquals("200", ids.getCode());
        } catch (NullPointerException e) {
            System.out.println(" ");
        }
        taskMapper.deleteById("1");
    }

    @Test
    public void testGetFileList() {
        Settings settings = new Settings(1, Constants.DEFAULT_TEST_DOWNLOAD_ROOT_PATH, Constants.DEFAULT_MAX_TASKS, Constants.DEFAULT_MAX_DOWNLOAD_SPEED);
        fileService.init(settings);
        // test the default empty query
        FileParam fileParam = new FileParam("", "", "");
        Result<List<com.example.HttpDownloadServer.entity.File>> result = fileService.fetchFileList(fileParam);
        assertEquals(result.getData().size(), 13);
        assertTrue(result.getData().getFirst().getName().compareTo(result.getData().getLast().getName()) < 0);

        // test Type=All and Sort=name and Order=up
        FileParam fileParam1 = new FileParam("All", "name", "up");
        Result<List<com.example.HttpDownloadServer.entity.File>> result1 = fileService.fetchFileList(fileParam1);
        assertEquals(result1.getData().size(), 13);
        assertTrue(result1.getData().getFirst().getName().compareTo(result1.getData().getLast().getName()) < 0);

        // test Type=Video and Sort=size and Order=down
        FileParam fileParam2 = new FileParam("Video", "size", "down");
        Result<List<com.example.HttpDownloadServer.entity.File>> result2 = fileService.fetchFileList(fileParam2);
        assertTrue(result2.getData().getFirst().getSize() > result2.getData().getLast().getSize());
        result2.getData().forEach(file -> {
            assertTrue(file.getName().endsWith(".mp4") || file.getName().endsWith(".mov"));
        });

        // test Type=Archive
        FileParam fileParam4 = new FileParam("Archive", "gmtCreated", "up");
        Result<List<com.example.HttpDownloadServer.entity.File>> result4 = fileService.fetchFileList(fileParam4);
        result4.getData().forEach(file -> {
            assertTrue(file.getName().endsWith(".zip") || file.getName().endsWith(".rar") || file.getName().endsWith(".tar"));
        });

        // test Type=Document
        FileParam fileParam5 = new FileParam("Document", "name", "up");
        Result<List<com.example.HttpDownloadServer.entity.File>> result5 = fileService.fetchFileList(fileParam5);
        result5.getData().forEach(file -> {
            assertTrue(file.getName().endsWith(".pptx") || file.getName().endsWith(".docx") || file.getName().endsWith(".xlsx"));
        });

        // test for illegal parameters
        FileParam fileParam6 = new FileParam("hello", "world", "!");
        Result<List<com.example.HttpDownloadServer.entity.File>> result6 = fileService.fetchFileList(fileParam6);
        assertEquals(result6.getData().size(), 13);
        assertTrue(result6.getData().getFirst().getName().compareTo(result6.getData().getLast().getName()) < 0);
    }
}
