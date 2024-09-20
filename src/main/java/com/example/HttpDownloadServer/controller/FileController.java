package com.example.HttpDownloadServer.controller;

import com.example.HttpDownloadServer.entity.File;
import com.example.HttpDownloadServer.param.FileParams;
import com.example.HttpDownloadServer.param.ResFileParams;
import com.example.HttpDownloadServer.service.FileService;
import com.example.HttpDownloadServer.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private FileService fileService;

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @ResponseBody
    @PostMapping("/list")
    public Result<List<File>> getFileList(FileParams params) {
        return fileService.fetchFileList(params);
    }
}
