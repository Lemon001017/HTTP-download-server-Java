package com.example.HttpDownloadServer;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.mapper.SettingsMapper;
import com.example.HttpDownloadServer.mapper.TaskMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class HttpDownloadServerDAOTests {
    @Autowired
    private SettingsMapper settingsMapper;
    @Autowired
    private TaskMapper taskMapper;

    @Test
    public void testUpdateSettings() {
        settingsMapper.deleteById(1);
        Settings settings = new Settings(1, "test", 2, 3.0);
        int result = settingsMapper.insert(settings);
        assertEquals(1, result);

        settings.setMaxTasks(0);
        settings.setMaxDownloadSpeed(-1);
        settings.setDownloadPath("");
        result = settingsMapper.updateById(settings);
        assertEquals(1, result);
        settingsMapper.deleteById(1);
    }

    @Test
    public void testGetSettings() {
        settingsMapper.deleteById(1);
        Settings settings = new Settings(1, "test", 2, 3);
        settingsMapper.insert(settings);
        Settings settings1 = settingsMapper.selectById(1);
        assertEquals("test", settings1.getDownloadPath());
        assertEquals(2, settings1.getMaxTasks());
        assertEquals(3, settings1.getMaxDownloadSpeed());
        settingsMapper.deleteById(1);
    }

    @Test
    public void testAddTask() {
        taskMapper.deleteById(1);
        Task task = new Task(
                "1", "test", "test",
                10L, 10, "test", "test", "test",
                1, 1.0, 1.0, 1.0, 1, 1
        );
        int result = taskMapper.insert(task);
        assertEquals(1, result);
        taskMapper.deleteById(1);
    }

    @Test
    public void testGetTasksById() {
        taskMapper.deleteByIds(List.of("1", "2"));
        Task task1 = new Task(
                "1", "test", "test",
                10L, 10, "test", "test", "test",
                1, 1.0, 1.0, 1.0, 1, 1
        );

        Task task2 = new Task(
                "2", "test2", "test2", 12L, 5, "test2", "test2",
                "test2", 1, 1.0, 1.0, 1.0, 1, 1
        );
        int res = taskMapper.insert(task1);
        res = taskMapper.insert(task2);
        List<Task> tasks = taskMapper.selectBatchIds(List.of("1", "2"));
        assertEquals(2, tasks.size());
        taskMapper.deleteByIds(List.of("1", "2"));
    }

    @Test
    public void testGetTasksByStatus() {
        taskMapper.deleteByIds(List.of("1", "2"));
        Task task1 = new Task();
        task1.setId("1");
        task1.setStatus(Constants.TASK_STATUS_DOWNLOADING);
        task1.setUrl("url1");
        taskMapper.insert(task1);
        Task task2 = new Task();
        task2.setId("2");
        task2.setStatus(Constants.TASK_STATUS_DOWNLOADED);
        task2.setUrl("url2");
        taskMapper.insert(task2);

        List<Task> tasks = taskMapper.getTasksByStatus(Constants.TASK_STATUS_DOWNLOADING);
        assertEquals(1, tasks.size());
        assertEquals("url1", tasks.getFirst().getUrl());

        tasks = taskMapper.getTasksByStatus(Constants.TASK_STATUS_CANCELED);
        assertEquals(0, tasks.size());
        taskMapper.deleteByIds(List.of("1", "2"));
    }

    @Test
    public void testDeleteTasksByIds() {
        taskMapper.deleteByIds(List.of("1", "2"));
        Task task1 = new Task();
        task1.setId("1");
        task1.setStatus(Constants.TASK_STATUS_DOWNLOADING);
        task1.setUrl("url1");
        taskMapper.insert(task1);
        Task task2 = new Task();
        task2.setId("2");
        task2.setStatus(Constants.TASK_STATUS_DOWNLOADED);
        task2.setUrl("url2");
        taskMapper.insert(task2);

        List<Task> tasks = taskMapper.selectBatchIds(List.of("1", "2"));
        assertEquals(2, tasks.size());

        taskMapper.deleteByIds(List.of("1", "2"));
        tasks = taskMapper.selectBatchIds(List.of("1", "2"));
        assertEquals(0, tasks.size());
    }
}