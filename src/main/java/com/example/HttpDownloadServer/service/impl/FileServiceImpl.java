package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.exception.StorageException;
import com.example.HttpDownloadServer.param.FileParams;
import com.example.HttpDownloadServer.service.FileService;
import com.example.HttpDownloadServer.utils.Result;
import org.apache.tomcat.jni.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileServiceImpl implements FileService {
    private static Path rootLocation;
    private static final Logger LOG = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public Result<List<com.example.HttpDownloadServer.entity.File>> fetchFileList(FileParams params) {
        Result<List<com.example.HttpDownloadServer.entity.File>> result = new Result<>();
        // 默认参数处理
        params.disposalFileParams();
        try {
            //获得下载路径下的深度为一的所有文件夹与文件
            Stream<Path> pathStream = Files.walk(rootLocation, 1)
                    // 去掉下载路径的文件夹
                    .filter(path -> !path.equals(rootLocation))
                    // 去掉非文件
                    .filter(path -> !Files.isDirectory(path))
                    // 返回处理成相对路径的Path流
                    .map(rootLocation::relativize);
            // 将 Path流转换为File流
            Stream<File> fileStream = pathStream.map(path -> new File(String.valueOf(rootLocation.resolve(path))));
            // 将File流数据转换为筛选后的文件列表
            ArrayList<com.example.HttpDownloadServer.entity.File> fileList = filterFilesByType(fileStream, params);
            // 关闭流
            pathStream.close();
            fileStream.close();
            result.setCode(Constants.HTTP_STATUS_OK);
            return result.setData(fileList);
        } catch (IOException e) {
            throw new StorageException(Constants.STORAGE_READ_ERROR, e);
        }
    }

    /**
     * 流处理，进行过滤、提取、排序、整理
     **/
    private ArrayList<com.example.HttpDownloadServer.entity.File> filterFilesByType(Stream<File> fileList, FileParams fileParams) {
        //
        return fileList.filter(file -> switch (fileParams.getType()) {
                    case "All" -> true; // 如果类型为 All，则返回所有文件
                    case "Video" -> file.getName().endsWith(".mp4") || file.getName().endsWith(".mov");
                    case "Photo" ->
                            file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".gif");
                    case "Archive" ->
                            file.getName().endsWith(".zip") || file.getName().endsWith(".rar") || file.getName().endsWith(".tar");
                    case "Document" ->
                            file.getName().endsWith(".pptx") || file.getName().endsWith(".docx") || file.getName().endsWith(".xlsx");
                    default -> false; // 默认情况下返回 false
                })
                .map(file -> new com.example.HttpDownloadServer.entity.File(
                        file.getName(), file.getPath().substring(18), file.length(), new Date(file.lastModified())
                ))
                .sorted((com.example.HttpDownloadServer.entity.File f1, com.example.HttpDownloadServer.entity.File f2) -> switch (fileParams.getSort()) {
                    case "name" ->
                            (fileParams.getOrder().equals("up")) ? f1.getName().compareTo(f2.getName()) : f2.getName().compareTo(f1.getName());
                    case "size" ->
                            (fileParams.getOrder().equals("up")) ? Long.compare(f1.getSize(), f2.getSize()) : Long.compare(f2.getSize(), f1.getSize());
                    case "gmtCreated" ->
                            (fileParams.getOrder().equals("up")) ? Long.compare(f1.getGmtModified().getTime(), f2.getGmtModified().getTime()) : Long.compare(f2.getGmtModified().getTime(), f1.getGmtModified().getTime());
                    default -> 0;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * init storage
     **/
    @Override
    public void init(Settings settings) {
        try {
            rootLocation = Paths.get(settings.getDownloadPath());
            // 创建下载路径文件夹,若该文件夹已经存在则不做任何操作
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException(Constants.STORAGE_INIT_ERROR, e);
        }
    }
}
