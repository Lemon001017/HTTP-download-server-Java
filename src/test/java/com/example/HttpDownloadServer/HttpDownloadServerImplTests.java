package com.example.HttpDownloadServer;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.mapper.SettingsMapper;
import com.example.HttpDownloadServer.service.RedisService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
public class HttpDownloadServerImplTests {

    private final RedisService redisService;
    private final SettingsMapper settingsMapper;

    @Autowired
    HttpDownloadServerImplTests(RedisService redisService, SettingsMapper settingsMapper){
        this.redisService=redisService;
        this.settingsMapper=settingsMapper;
    }

    @BeforeEach
    public void initRedis() {
        for(int j=0;j<10;j++) {
            redisService.initializeScoreboard(Constants.KEY_CHUNK_STRING_PREFIX + j, 10);
            for (int i = 0; i < 5; i++) {
                redisService.updateScoreboard(Constants.KEY_CHUNK_STRING_PREFIX + j, i);
            }
        }

    }
    @AfterEach
    public void clearRedis() {
        for(int j=0;j<10;j++) {
            redisService.deleteScoreboard(Constants.KEY_CHUNK_STRING_PREFIX+j);
        }
    }
    @Test
    public void testGet() {
        for(int j=0;j<10;j++) {
            int[] ints = {5, 6, 7, 8, 9};
            List<Integer> list = Arrays.stream(ints).boxed().toList();
            Assertions.assertEquals(list, redisService.getScoreboard(Constants.KEY_CHUNK_STRING_PREFIX+j));
        }
    }

    @Test
    public void testNormalAddTask() {
        Task task = new Task(
                "2", "test", "test",
                10L, 10, "test", "test", "downloaded",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        Settings settings = new Settings(2, Constants.DEFAULT_DOWNLOAD_ROOT_PATH, Constants.DEFAULT_MAX_TASKS, Constants.DEFAULT_MAX_DOWNLOAD_SPEED);
        settingsMapper.insert(settings);
        Assertions.assertTrue(redisService.addTaskQueue(task));
        Assertions.assertTrue(redisService.deleteTaskQueue(task));
        settingsMapper.deleteById(2);
    }

    @Test
    public void testUnnormalAddTask() throws InterruptedException {
        Settings settings = new Settings(2, Constants.DEFAULT_DOWNLOAD_ROOT_PATH, Constants.DEFAULT_MAX_TASKS, Constants.DEFAULT_MAX_DOWNLOAD_SPEED);
        settingsMapper.insert(settings);
        Task task1 = new Task(
                "1", "test", "test",
                10L, 10, "test", "test", "downloaded",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        Task task2 = new Task(
                "2", "test", "test",
                10L, 10, "test", "test", "downloaded",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        Task task3 = new Task(
                "3", "test", "test",
                10L, 10, "test", "test", "downloaded",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        Task task4 = new Task(
                "4", "test", "test",
                10L, 10, "test", "test", "downloaded",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        Task task5 = new Task(
                "5", "test", "test",
                10L, 10, "test", "test", "downloaded",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        Task task6 = new Task(
                "6", "test", "test",
                10L, 10, "test", "test", "downloaded",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );

        Assertions.assertTrue(redisService.addTaskQueue(task1));
        Assertions.assertTrue(redisService.addTaskQueue(task2));
        Assertions.assertTrue(redisService.addTaskQueue(task3));
        Assertions.assertTrue(redisService.addTaskQueue(task4));
        Assertions.assertFalse(redisService.addTaskQueue(task5));
        Assertions.assertEquals(Constants.TASK_STATUS_CANCELED,task5.getStatus());
        Thread thread=new Thread(()-> Assertions.assertTrue(redisService.addTaskQueue(task6)));
        thread.start();
        Thread.sleep(1000);
        Assertions.assertTrue(redisService.deleteTaskQueue(task1));
        Thread.sleep(1000);
        Assertions.assertTrue(redisService.deleteTaskQueue(task2));
        Assertions.assertTrue(redisService.deleteTaskQueue(task3));
        Assertions.assertTrue(redisService.deleteTaskQueue(task4));
        Assertions.assertFalse(redisService.deleteTaskQueue(task5));
        Assertions.assertTrue(redisService.deleteTaskQueue(task6));
        settingsMapper.deleteById(2);
    }
}
