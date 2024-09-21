package com.example.HttpDownloadServer;

import com.example.HttpDownloadServer.service.RedisService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class HttpDownloadServerImplTests {
@Autowired
    private RedisService redisService;

    @BeforeEach
    public void initRedis() {
        long st=System.currentTimeMillis();
        redisService.initializeScoreboard("test", 300);
        for(int i = 0; i < 100; i++){
            redisService.updateScoreboard("test", i);
        }
        long en=System.currentTimeMillis();
        System.out.println(en-st);
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
}
