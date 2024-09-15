package com.example.HttpDownloadServer.controller;

import com.example.HttpDownloadServer.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileController {
    @Autowired
    private FileService fileService;
}
