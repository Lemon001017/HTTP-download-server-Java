package com.example.HttpDownloadServer.controller;

import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.param.Result;
import com.example.HttpDownloadServer.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * Submit a task for download
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestParam String url) throws IOException, URISyntaxException {
        return taskService.submit(url);
    }

    /**
     * Pause tasks
     */
    @PostMapping("/pause")
    public Result<List<String>> pause(@RequestBody List<String> ids) {
        return taskService.pause(ids);
    }

    /**
     * Resume tasks
     */
    @PostMapping("/resume")
    public Result<List<String>> resume(@RequestBody List<String> ids) {
        return taskService.resume(ids);
    }

    /**
     * Restart tasks
     */
    @PostMapping("/restart")
    public Result<List<String>> restart(@RequestBody List<String> ids) {
        return taskService.restart(ids);
    }

    /**
     * Delete tasks
     */
    @PostMapping("/delete")
    public Result<List<String>> delete(@RequestBody List<String> ids) {
        return taskService.delete(ids);
    }

    /**
     * Get task list
     */
    @PostMapping("/list")
    public Result<List<Task>> getTaskList(@RequestParam String status) {
        return taskService.getTaskList(status);
    }
}
