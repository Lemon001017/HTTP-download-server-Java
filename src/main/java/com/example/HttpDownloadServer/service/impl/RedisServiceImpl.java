package com.example.HttpDownloadServer.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.exception.DownloadException;
import com.example.HttpDownloadServer.mapper.SettingsMapper;
import com.example.HttpDownloadServer.mapper.TaskMapper;
import com.example.HttpDownloadServer.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final SettingsMapper settingsMapper;
    private final TaskMapper taskMapper;
    private final Random random = new Random();
    private static final Logger LOG = LoggerFactory.getLogger(RedisServiceImpl.class);



    @Autowired
    public RedisServiceImpl(TaskMapper taskMapper,SettingsMapper settingsMapper,RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.settingsMapper=settingsMapper;
        this.taskMapper=taskMapper;
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
    @Override
    @Retryable(retryFor = {DownloadException.class}, maxAttempts = Constants.DEFAULT_MAX_ATTEMPTS)
    public boolean addTaskQueue(Task task){
        Settings settings= settingsMapper.selectById(1);
            ListOperations<String, String> listOperations= redisTemplate.opsForList();
            Long beforeSize=listOperations.size(Constants.KEY_WORK_QUEUE);
                if (beforeSize >= settings.getMaxTasks()) {
                    throw new DownloadException(Constants.TASK_QUEUE_FULL);
                }
            Long afterSize=listOperations.leftPush(Constants.KEY_WORK_QUEUE, JSON.toJSONString(task));
            LOG.info("add task to work queue, taskId: {}",task.getId());
            return beforeSize<afterSize;
    }

    @Recover
    public boolean recover(DownloadException e, Task task) {
        task.setStatus(Constants.TASK_STATUS_CANCELED);
        taskMapper.updateById(task);
        return false;
    }
    @Override
    public boolean deleteTaskQueue(Task task){
        ListOperations<String, String> listOperations= redisTemplate.opsForList();
        return listOperations.remove(Constants.KEY_WORK_QUEUE, 1, JSON.toJSONString(task))>0L;
    }
}
