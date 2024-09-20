package com.example.HttpDownloadServer.service;

import java.util.List;

public interface RedisService {
    void initializeScoreboard(String taskId, long chunkNum);

    void updateScoreboard(String taskId, long chunkId);

    List<Long> getScoreboard(String taskId);
}
