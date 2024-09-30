package com.example.HttpDownloadServer.service;

import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.utils.Result;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    Result<SseEmitter> subscribe(String id);

    void send(String id, Task task);

    void close(String id);
}
