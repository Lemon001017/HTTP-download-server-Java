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
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {
    //    @Autowired
    private final RedisTemplate<String, String> redisTemplate;
    //    @Autowired
    private final TaskMapper taskMapper;
    //    @Autowired
    private final SettingsMapper settingsMapper;
    private final Random random = new Random();
    private final Object lock = new Object();
    private static final Logger LOG = LoggerFactory.getLogger(RedisServiceImpl.class);


    @Autowired
    public RedisServiceImpl(TaskMapper taskMapper, SettingsMapper settingsMapper, RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.taskMapper = taskMapper;
        this.settingsMapper = settingsMapper;
    }

    @Override
    public void initializeScoreboard(String taskId, int chunkNum) {
        ConcurrentHashMap<String, Boolean> scoreboard = new ConcurrentHashMap<>(chunkNum);
        for (int i = 0; i < chunkNum; i++) {
            scoreboard.put(String.valueOf(i), false);
        }
        redisTemplate.opsForValue().set(Constants.KEY_CHUNK_STRING_PREFIX + taskId, JSON.toJSONString(scoreboard), Constants.KEY_EXPIRE_MINUTES+random.nextInt(60), TimeUnit.MINUTES);
    }

    @Override
    public void updateScoreboard(String taskId, int chunkId) {
        ConcurrentHashMap<String, Boolean> scoreboard = JSON.parseObject(redisTemplate.opsForValue().get(Constants.KEY_CHUNK_STRING_PREFIX + taskId), new TypeReference<>() {
        });
        if (scoreboard != null) {
            scoreboard.put(String.valueOf(chunkId), true);
            redisTemplate.opsForValue().set(Constants.KEY_CHUNK_STRING_PREFIX + taskId, JSON.toJSONString(scoreboard),Constants.KEY_EXPIRE_MINUTES+random.nextInt(60), TimeUnit.MINUTES);
        }
    }

    @Override
    public List<Integer> getScoreboard(String taskId) {
        List<Integer> chunkIds = new ArrayList<>();
        ConcurrentHashMap<String, Boolean> scoreboard = JSON.parseObject(redisTemplate.opsForValue().get(Constants.KEY_CHUNK_STRING_PREFIX + taskId), new TypeReference<>() {
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
        redisTemplate.opsForValue().getAndDelete(Constants.KEY_CHUNK_STRING_PREFIX + taskId);
    }

    @Override
    public boolean addTaskQueue(Task task) {
        Settings settings = settingsMapper.selectById(1);
        int retryCount = 0;
        while (retryCount++ < Constants.DEFAULT_MAX_ATTEMPTS) {
            ListOperations<String, String> listOperations = redisTemplate.opsForList();
            Long beforeSize = listOperations.size(Constants.KEY_WORK_QUEUE);
            if (beforeSize != null) {
                if (beforeSize >= settings.getMaxTasks()) {
                    try {
                        Thread.sleep(Constants.DEFAULT_BACKOFF_MILLIS);
                    } catch (InterruptedException e) {
                        throw new DownloadException(Constants.TASK_QUEUE_SLEEP_ERROR);
                    }
                    continue;
                }
                synchronized (lock) {
                    Long afterSize = listOperations.leftPush(Constants.KEY_WORK_QUEUE, JSON.toJSONString(task));
                    if (afterSize != null) {
                        LOG.info("add task to work queue, taskId: {}", task.getId());
                        return afterSize > 0L;
                    }
                }
                // redis内无任务队列时，直接放入队列
            } else {
                synchronized (lock) {
                    Long afterSize = listOperations.leftPush(Constants.KEY_WORK_QUEUE, JSON.toJSONString(task));
                    if (afterSize != null) {
                        return afterSize > 0L;
                    }
                }
            }
            LOG.error("add task to work queue failed, taskId: {}", task.getId());
            return false;
        }
        task.setStatus(Constants.TASK_STATUS_CANCELED);
        taskMapper.updateById(task);
        return false;
    }

    @Override
    public boolean deleteTaskQueue(Task task) {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        Long size = listOperations.size(Constants.KEY_WORK_QUEUE);
        if (size != null) {
            Long deletedSize = listOperations.remove(Constants.KEY_WORK_QUEUE, 0, JSON.toJSONString(task));
            if (deletedSize != null) {
                return deletedSize > 0L;
            }
        }
        return false;
    }
}
