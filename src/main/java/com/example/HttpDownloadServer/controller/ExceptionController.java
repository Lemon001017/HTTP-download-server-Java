package com.example.HttpDownloadServer.controller;

import com.example.HttpDownloadServer.config.HTTPStatusConfig;
import com.example.HttpDownloadServer.exception.DownloadException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExceptionController {
    @ResponseBody
    @PostMapping("/test")
    public String testException() {
        throw new DownloadException(HTTPStatusConfig.DOWNLOAD_TIMEOUT);
    }
}
