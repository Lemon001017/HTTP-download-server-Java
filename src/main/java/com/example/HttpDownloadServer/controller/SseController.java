package com.example.HttpDownloadServer.controller;

import com.example.HttpDownloadServer.service.SseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/event")
public class SseController {

    @Autowired
    private SseService sseService;

    @GetMapping("/{taskId}")
    public SseEmitter handleSse(@PathVariable String taskId) {
        return sseService.subscribe(taskId).getData();
    }
}
