package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RedisServiceImpl implements RedisService {
    @Autowired
    private RedisTemplate redisTemplate;



    @Override
    public void initializeScoreboard(String taskId, long chunkNum){
        Map<String,Boolean> scoreboard=new HashMap<>();
        for (long i = 0;i < chunkNum; i++){
            scoreboard.put(String.valueOf(i),false);
        }
        redisTemplate.opsForHash().put(Constants.KEY_CHUNK_HASHMAP,taskId,scoreboard);
    }

    @Override
    public void updateScoreboard(String taskId, long chunkId) {
        Map<String,Boolean> scoreboard=(Map) redisTemplate.opsForHash().get(Constants.KEY_CHUNK_HASHMAP,taskId);
        scoreboard.put(String.valueOf(chunkId),true);
        redisTemplate.opsForHash().put(Constants.KEY_CHUNK_HASHMAP,taskId,scoreboard);
    }

    @Override
    public List<Long> getScoreboard(String taskId){
        List<Long> chunkIds=new ArrayList<>();
        Map<String,Boolean> scoreboard=(Map) redisTemplate.opsForHash().get(Constants.KEY_CHUNK_HASHMAP,taskId);
        scoreboard.forEach((key,value)->{
            if (!value){
                chunkIds.add(Long.valueOf(key));
            }
        });
        return chunkIds;
    }
}
