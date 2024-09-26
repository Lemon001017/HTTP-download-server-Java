package com.example.HttpDownloadServer.service;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.exception.DownloadException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.List;

public interface RedisService {

    void initializeScoreboard(String taskId, int chunkNum);


    void updateScoreboard(String taskId, int chunkId);


    List<Integer> getScoreboard(String taskId);


    void deleteScoreboard(String taskId);


    boolean addTaskQueue(Task task);

    boolean deleteTaskQueue(Task task);

}