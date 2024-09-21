package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RedisServiceImpl implements RedisService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void initializeScoreboard(String taskId, int chunkNum) {
        Map<Integer,Boolean> scoreboard=new LinkedHashMap<>(chunkNum);
        for (int i = 0;i < chunkNum; i++){
            scoreboard.put(i,false);
        }
        System.out.println(scoreboard);
        redisTemplate.opsForHash().put(Constants.KEY_CHUNK_HASHMAP,taskId,scoreboard);
    }
    @Override
    public void updateScoreboard(String taskId, int chunkId) {
        LinkedHashMap<Integer,Boolean> scoreboard=(LinkedHashMap) redisTemplate.opsForHash().get(Constants.KEY_CHUNK_HASHMAP,taskId);
        if (scoreboard != null) {
            scoreboard.put(chunkId,true);
            redisTemplate.opsForHash().put(Constants.KEY_CHUNK_HASHMAP,taskId,scoreboard);
        }
    }
    @Override
    public List<Integer> getScoreboard(String taskId) {
        List<Integer> chunkIds=new ArrayList<>();
        List<Integer> synchronizedList = Collections.synchronizedList(chunkIds);
        LinkedHashMap<Integer,Boolean> scoreboard=(LinkedHashMap) redisTemplate.opsForHash().get(Constants.KEY_CHUNK_HASHMAP,taskId);
        if (scoreboard != null) {
            scoreboard.forEach((key,value)->{
                if (!value){
                    synchronizedList.add(key);
                }
            });
        }
        return synchronizedList;
    }

    @Override
    public void deleteScoreboard(String taskId) {
        redisTemplate.opsForHash().delete(Constants.KEY_CHUNK_HASHMAP, taskId);
    }
}
