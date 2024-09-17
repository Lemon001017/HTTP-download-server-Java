package com.example.HttpDownloadServer.controller;

import com.example.HttpDownloadServer.entity.File;
import com.example.HttpDownloadServer.param.FileParams;
import com.example.HttpDownloadServer.service.FileService;
import com.example.HttpDownloadServer.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FileController {
    @Autowired
    private FileService fileService;
    @ResponseBody
    @PostMapping("getFileList")
    public Result<List<File>> getFileList(FileParams params) {
        return fileService.fetchFileList(params);
    }
}
