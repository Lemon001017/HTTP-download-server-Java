package com.example.HttpDownloadServer.controller;

import com.example.HttpDownloadServer.service.SseService;
import com.example.HttpDownloadServer.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event")
public class SseController {
    @Autowired
    private SseService sseService;

    @GetMapping("{id}")
    public Result<String> handleSse(@PathVariable String id) {
        return sseService.handleSse(id);
    }
}
