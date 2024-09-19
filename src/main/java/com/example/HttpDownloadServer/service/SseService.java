package com.example.HttpDownloadServer.service;

import com.example.HttpDownloadServer.utils.Result;

public interface SseService {
    public Result<String> handleSse(String id);
}
