package com.example.HttpDownloadServer.controller;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.exception.DownloadException;
import com.example.HttpDownloadServer.service.impl.MessageReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExceptionController {
    @Autowired
    private MessageSender messageSender;
    @Autowired
    private MessageReceiver messageReceiver;

    @ResponseBody
    @PostMapping("/test")
    public String testException() {
        throw new DownloadException(Constants.DOWNLOAD_TIMEOUT);
    }

}
