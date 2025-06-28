package com.example.HttpDownloadServer.controller;

import com.example.HttpDownloadServer.entity.File;
import com.example.HttpDownloadServer.param.FileParam;
import com.example.HttpDownloadServer.service.FileService;
import com.example.HttpDownloadServer.param.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/list")
    public Result<List<File>> getFileList(@RequestBody FileParam params) {
        return fileService.fetchFileList(params);
    }
}
