package com.example.HttpDownloadServer.service;

import com.example.HttpDownloadServer.entity.Task;
import reactor.core.publisher.Flux;

public interface SseService {
    Flux<Task> subscribe(String id);

    void send(String id, Task task);

    void close(String id);
}
