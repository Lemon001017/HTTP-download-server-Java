package com.example.HttpDownloadServer.service;

import com.example.HttpDownloadServer.entity.File;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.param.FileParams;
import com.example.HttpDownloadServer.utils.Result;

import java.util.List;

public interface FileService {
    Result<List<File>> fetchFileList(FileParams params);


    void init(Settings settings);
}
