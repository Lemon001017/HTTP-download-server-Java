package com.example.HttpDownloadServer.service;

import com.example.HttpDownloadServer.entity.File;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.param.FileParam;
import com.example.HttpDownloadServer.param.Result;

import java.util.List;

public interface FileService {
    Result<List<File>> fetchFileList(FileParam params);

    void init(Settings settings);
}
