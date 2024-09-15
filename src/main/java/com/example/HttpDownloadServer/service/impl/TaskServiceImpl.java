package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.service.TaskService;
import com.example.HttpDownloadServer.utils.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    @Override
    public Result<String> submit(String url) {
        return null;
    }

    @Override
    public Result<List<String>> pause(List<String> ids) {
        return null;
    }

    @Override
    public Result<List<String>> resume(List<String> ids) {
        return null;
    }

    @Override
    public Result<List<String>> restart(List<String> ids) {
        return null;
    }

    @Override
    public Result<List<String>> delete(List<String> ids) {
        return null;
    }

    @Override
    public Result<List<Task>> getTaskList(String status) {
        return null;
    }
}
