package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.service.SseService;
import com.example.HttpDownloadServer.utils.Result;
import org.springframework.stereotype.Service;

@Service
public class SseServiceImpl implements SseService {
    @Override
    public Result<String> handleSse(String id) {
        return null;
    }
}
