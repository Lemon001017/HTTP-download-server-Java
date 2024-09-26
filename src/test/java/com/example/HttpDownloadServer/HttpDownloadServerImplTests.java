package com.example.HttpDownloadServer;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.exception.DownloadException;
import com.example.HttpDownloadServer.mapper.SettingsMapper;
import com.example.HttpDownloadServer.service.RedisService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.ExhaustedRetryException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        redisService.initializeScoreboard("test", 10);
        for(int i = 0; i < 5; i++){
            redisService.updateScoreboard("test", i);
        }

    }
    @AfterEach
    public void clearRedis() {
        redisService.deleteScoreboard("test");

    }
    @Test
    public void testGet() {
        long time1 = System.currentTimeMillis();
        redisService.getScoreboard("test");
        int[] ints={5,6,7,8,9};
        List<Integer> list= Arrays.stream(ints).boxed().toList();
        Assertions.assertEquals(list, redisService.getScoreboard("test"));
    }

    @Test
    public void testNormalAddTask() {
        Task task = new Task(
                "1", "test", "test",
                10L, 10, "test", "test", "downloaded",
                1, 1.0, 1.0, 1.0, 1, 1, LocalDateTime.now()
        );
        Settings settings = new Settings(1, Constants.DEFAULT_DOWNLOAD_ROOT_PATH, Constants.DEFAULT_MAX_TASKS, Constants.DEFAULT_MAX_DOWNLOAD_SPEED);
        settingsMapper.insert(settings);
        Assertions.assertTrue(redisService.addTaskQueue(task));
        Assertions.assertTrue(redisService.deleteTaskQueue(task));
        settingsMapper.deleteById(1);
    }

    @Test
    public void testUnnormalAddTask() throws InterruptedException {
        Settings settings = new Settings(1, Constants.DEFAULT_DOWNLOAD_ROOT_PATH, Constants.DEFAULT_MAX_TASKS, Constants.DEFAULT_MAX_DOWNLOAD_SPEED);
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
        Thread thread=new Thread(()->{
            try {
                Assertions.assertTrue(redisService.addTaskQueue(task6));
            } catch (DownloadException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        Thread.sleep(1000);
        Assertions.assertTrue(redisService.deleteTaskQueue(task1));
        Thread.sleep(1000);
        Assertions.assertTrue(redisService.deleteTaskQueue(task2));
        Assertions.assertTrue(redisService.deleteTaskQueue(task3));
        Assertions.assertTrue(redisService.deleteTaskQueue(task4));
        Assertions.assertFalse(redisService.deleteTaskQueue(task5));
        Assertions.assertTrue(redisService.deleteTaskQueue(task6));
        settingsMapper.deleteById(1);
    }
}
