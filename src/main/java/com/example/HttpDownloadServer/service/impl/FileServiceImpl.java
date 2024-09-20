package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.exception.StorageException;
import com.example.HttpDownloadServer.param.FileParams;
import com.example.HttpDownloadServer.service.FileService;
import com.example.HttpDownloadServer.utils.Result;
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
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public Result<List<com.example.HttpDownloadServer.entity.File>> fetchFileList(FileParams params) {
        Result<List<com.example.HttpDownloadServer.entity.File>> result = new Result<>();
        params.disposalFileParams();
        try ( Stream<Path> pathStream= Files.walk(rootLocation, 1)){
            // Get all folders and files with depth one in the download path
            List<com.example.HttpDownloadServer.entity.File> fileList= filterFilesByType(pathStream, params);
            result.setCode(Constants.HTTP_STATUS_OK);
            result.setData(fileList);
        } catch (IOException e) {
            log.error("Resource read failed");
            throw new StorageException(Constants.STORAGE_READ_ERROR, e);
        }
        return result;
    }

    /**
     * Stream processing, parsing, filtering, extraction, sorting, sorting
     **/
    private ArrayList<com.example.HttpDownloadServer.entity.File> filterFilesByType(Stream<Path> pathStream, FileParams params) {
        return pathStream
                .filter(path -> !path.equals(rootLocation))
                .filter(path -> !Files.isDirectory(path))
                .map(rootLocation::relativize)
                .map(path -> new File(String.valueOf(rootLocation.resolve(path))))
                .filter(file -> switch (params.getType()) {
                    case "Video" -> file.getName().endsWith(".mp4") || file.getName().endsWith(".mov");
                    case "Photo" ->
                            file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".gif");
                    case "Archive" ->
                            file.getName().endsWith(".zip") || file.getName().endsWith(".rar") || file.getName().endsWith(".tar");
                    case "Document" ->
                            file.getName().endsWith(".pptx") || file.getName().endsWith(".docx") || file.getName().endsWith(".xlsx");
                    default -> true;
                })
                .map(file -> new com.example.HttpDownloadServer.entity.File(
                        file.getName(), file.getPath().substring(18), file.length(), new Date(file.lastModified())
                ))
                .sorted((com.example.HttpDownloadServer.entity.File f1, com.example.HttpDownloadServer.entity.File f2) -> switch (params.getSort()) {
                    case "name" ->
                            (params.getOrder().equals("up")) ? f1.getName().compareTo(f2.getName()) : f2.getName().compareTo(f1.getName());
                    case "size" ->
                            (params.getOrder().equals("up")) ? Long.compare(f1.getSize(), f2.getSize()) : Long.compare(f2.getSize(), f1.getSize());
                    case "gmtCreated" ->
                            (params.getOrder().equals("up")) ? Long.compare(f1.getGmtModified().getTime(), f2.getGmtModified().getTime()) : Long.compare(f2.getGmtModified().getTime(), f1.getGmtModified().getTime());
                    default -> 0;
                }).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * init storage
     **/
    @Override
    public void init(Settings settings) {
        try {
            rootLocation = Paths.get(settings.getDownloadPath());
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            log.error("Storage initialization failed");
            throw new StorageException(Constants.STORAGE_INIT_ERROR, e);
        }
    }
}
