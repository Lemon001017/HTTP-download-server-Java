package com.example.HttpDownloadServer.controller;

import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.service.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/event")
public class SseController {

    @Autowired
    private SseService sseService;

    @GetMapping(value = "/{taskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Task> handleSse(@PathVariable String taskId) {
        return sseService.subscribe(taskId);
    }
}
