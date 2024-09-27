package com.example.HttpDownloadServer.service;

import com.example.HttpDownloadServer.entity.Task;

import java.util.List;

public interface RedisService {

    void initializeScoreboard(String taskId, int chunkNum);


    void updateScoreboard(String taskId, int chunkId);


    List<Integer> getScoreboard(String taskId);


    void deleteScoreboard(String taskId);


    boolean addTaskQueue(Task task);

    boolean deleteTaskQueue(Task task);

}