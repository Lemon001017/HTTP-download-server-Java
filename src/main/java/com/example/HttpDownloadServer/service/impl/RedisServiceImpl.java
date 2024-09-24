package com.example.HttpDownloadServer.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final Random random = new Random();

    @Autowired
    public RedisServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void initializeScoreboard(String taskId, int chunkNum) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        ConcurrentHashMap<String, Boolean> scoreboard = new ConcurrentHashMap<>(chunkNum);
        for (int i = 0; i < chunkNum; i++) {
            scoreboard.put(String.valueOf(i), false);
        }
        hashOps.put(Constants.KEY_CHUNK_HASHMAP, taskId, JSON.toJSONString(scoreboard));
        redisTemplate.expire(Constants.KEY_CHUNK_HASHMAP, 60 + random.nextInt(20), TimeUnit.MINUTES);
    }

    @Override
    public void updateScoreboard(String taskId, int chunkId) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        ConcurrentHashMap<String, Boolean> scoreboard = JSON.parseObject(hashOps.get(Constants.KEY_CHUNK_HASHMAP, taskId), new TypeReference<>() {
        });

        if (scoreboard != null) {
            scoreboard.put(String.valueOf(chunkId), true);
            redisTemplate.opsForHash().put(Constants.KEY_CHUNK_HASHMAP, taskId, JSON.toJSONString(scoreboard));
        }
    }

    @Override
    public List<Integer> getScoreboard(String taskId) {
        List<Integer> chunkIds = new ArrayList<>();
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        ConcurrentHashMap<String, Boolean> scoreboard = JSON.parseObject(hashOps.get(Constants.KEY_CHUNK_HASHMAP, taskId), new TypeReference<>() {
        });
        if (scoreboard != null) {
            scoreboard.forEach((key, value) -> {
                if (!value) {
                    chunkIds.add(Integer.valueOf(key));
                }
            });
        }
        return chunkIds;
    }

    @Override
    public void deleteScoreboard(String taskId) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        if (hashOps.hasKey(Constants.KEY_CHUNK_HASHMAP, taskId)) {
            redisTemplate.opsForHash().delete(Constants.KEY_CHUNK_HASHMAP, taskId);
        }
    }
}
