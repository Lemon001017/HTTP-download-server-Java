package com.example.HttpDownloadServer;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.exception.DownloadException;
import com.example.HttpDownloadServer.service.RedisService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.ExhaustedRetryException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
@SpringBootTest
public class HttpDownloadServerImplTests {
@Autowired
    private RedisService redisService;

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
        Assertions.assertTrue(redisService.addTaskQueue(task));
        Assertions.assertTrue(redisService.deleteTaskQueue(task));
    }

    @Test
    public void testUnnormalAddTask() throws InterruptedException {
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
        Assertions.assertDoesNotThrow(()->redisService.addTaskQueue(task5));
        Thread.sleep(2000);
        Assertions.assertEquals(Constants.TASK_STATUS_CANCELED,task5.getStatus());
        Thread thread=new Thread(()->{
            try {
                System.out.println("666");
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
    }
}
