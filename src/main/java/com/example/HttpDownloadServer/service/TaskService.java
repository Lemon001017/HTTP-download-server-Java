package com.example.HttpDownloadServer.service;

import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.utils.Result;

import java.util.List;

public interface TaskService {
    public Result<String> submit(String url);

    public Result<List<String>> pause(List<String> ids);

    public Result<List<String>> resume(List<String> ids);

    public Result<List<String>> restart(List<String> ids);

    public Result<List<String>> delete(List<String> ids);

    public Result<List<Task>> getTaskList(String status);
}
