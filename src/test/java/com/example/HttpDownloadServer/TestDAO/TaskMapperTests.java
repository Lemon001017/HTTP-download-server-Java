package com.example.HttpDownloadServer.TestDAO;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.mapper.TaskMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TaskMapperTests {
    @Autowired
    private TaskMapper taskMapper;

    @Test
    public void testAddTask() {
        Task task = new Task("1", "test", "test", 1L, "test", "test",
                "test", 1, 1.0, 1.0, 1.0, 1, 1);
        int result = taskMapper.insert(task);
        assertEquals(1, result);
    }

    @Test
    public void testGetTasksById() {
        Task task1 = new Task("1", "test", "test", 10L, "test", "test",
                "test", 1, 1.0, 1.0, 1.0, 1, 1);
        Task task2 = new Task("2", "test2", "test2", 12L, "test2", "test2",
                "test2", 1, 1.0, 1.0, 1.0, 1, 1);
        taskMapper.insert(task1);
        taskMapper.insert(task2);
        List<String> ids = List.of("1", "2");
        List<Task> tasks = taskMapper.selectBatchIds(ids);
        assertEquals(2, tasks.size());
    }

    @Test
    public void testGetTasksByStatus() {
        Task task1 = new Task();
        task1.setStatus(Constants.TASK_STATUS_DOWNLOADING);
        task1.setUrl("url1");
        taskMapper.insert(task1);
        Task task2 = new Task();
        task2.setStatus(Constants.TASK_STATUS_DOWNLOADED);
        task2.setUrl("url2");
        taskMapper.insert(task2);

        List<Task> tasks = taskMapper.getTasksByStatus(Constants.TASK_STATUS_DOWNLOADING);
        assertEquals(1, tasks.size());
        assertEquals("url1", tasks.get(0).getUrl());

        tasks = taskMapper.getTasksByStatus(Constants.TASK_STATUS_CANCELED);
        assertEquals(0, tasks.size());
    }

    @Test
    public void testDeleteTasksByIds() {
        Task task1 = new Task();
        task1.setStatus(Constants.TASK_STATUS_DOWNLOADING);
        task1.setUrl("url1");
        taskMapper.insert(task1);
        Task task2 = new Task();
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
