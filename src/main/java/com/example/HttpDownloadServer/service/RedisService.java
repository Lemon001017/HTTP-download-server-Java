package com.example.HttpDownloadServer.service;

import java.util.List;

public interface RedisService {

    void initializeScoreboard(String taskId, int chunkNum);

    void updateScoreboard(String taskId, int chunkId);

    List<Integer> getScoreboard(String taskId);

    void deleteScoreboard(String taskId);
}