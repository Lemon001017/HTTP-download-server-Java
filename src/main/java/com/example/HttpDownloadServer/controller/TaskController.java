package com.example.HttpDownloadServer.controller;

import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.service.TaskService;
import com.example.HttpDownloadServer.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task")
public class TaskController {
    @Autowired
    private TaskService taskService;

    /**
     * Submit a task for download
     *
     * @param url
     * @return
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestParam String url) {
        return taskService.submit(url);
    }

    /**
     * Pause tasks
     *
     * @param ids
     * @return
     */
    @PostMapping("/pause")
    public Result<List<String>> pause(@RequestBody List<String> ids) {
        return taskService.pause(ids);
    }

    /**
     * Resume tasks
     *
     * @param ids
     * @return
     */
    @PostMapping("/resume")
    public Result<List<String>> resume(@RequestBody List<String> ids) {
        return taskService.resume(ids);
    }

    /**
     * Restart tasks
     *
     * @param ids
     * @return
     */
    @PostMapping("/restart")
    public Result<List<String>> restart(@RequestBody List<String> ids) {
        return taskService.restart(ids);
    }

    /**
     * Delete tasks
     *
     * @param ids
     * @return
     */
    @PostMapping("/delete")
    public Result<List<String>> delete(@RequestBody List<String> ids) {
        return taskService.delete(ids);
    }

    /**
     * Get task list
     *
     * @param status
     * @return
     */
    @GetMapping("/list")
    public Result<List<Task>> getTaskList(@RequestParam String status) {
        return taskService.getTaskList(status);
    }

}
