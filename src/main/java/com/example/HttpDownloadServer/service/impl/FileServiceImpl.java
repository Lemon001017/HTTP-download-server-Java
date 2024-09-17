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
import java.util.stream.Stream;

@Service
public class FileServiceImpl implements FileService {
    private static Path rootLocation;
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public Result<List<com.example.HttpDownloadServer.entity.File>> fetchFileList(FileParams params) {
        Result<List<com.example.HttpDownloadServer.entity.File>> result = new Result<>();
        params.disposalFileParams();
        try {
            Stream<Path> pathStream = Files.walk(rootLocation, 1)
                    // Remove the folder from the download path
                    .filter(path -> !path.equals(rootLocation))
                    .filter(path -> !Files.isDirectory(path))
                    .map(rootLocation::relativize);
            Stream<File> fileStream = pathStream.map(path -> new File(String.valueOf(rootLocation.resolve(path))));
            pathStream.close();
            ArrayList<File> fileList = filterFilesByType(fileStream, params.getType());
            sortFiles(fileList, params.getSort(), params.getOrder());
            List<com.example.HttpDownloadServer.entity.File> files = new ArrayList<>();

            fileList.forEach(otherFile -> {
                files.add(
                        new com.example.HttpDownloadServer.entity.File(
                                otherFile.getName(),
                                otherFile.getPath().substring(18),
                                otherFile.length(),
                                new Date(otherFile.lastModified())
                        )
                );
            });

            result.setCode(Constants.HTTP_STATUS_OK);
            return result.setData(files);
        } catch (IOException e) {
            log.error("Error reading file list: {}", e.getMessage());
            throw new StorageException(Constants.STORAGE_READ_ERROR, e);
        }
    }

    private ArrayList<File> filterFilesByType(Stream<File> fileList, String type) {
        return new ArrayList<>(fileList.filter(file -> switch (type) {
            case "All" -> true;
            case "Video" -> file.getName().endsWith(".mp4") || file.getName().endsWith(".mov");
            case "Photo" ->
                    file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".gif");
            case "Archive" ->
                    file.getName().endsWith(".zip") || file.getName().endsWith(".rar") || file.getName().endsWith(".tar");
            case "Document" ->
                    file.getName().endsWith(".pptx") || file.getName().endsWith(".docx") || file.getName().endsWith(".xlsx");
            default -> false;
        }).toList());
    }

    private void sortFiles(ArrayList<File> fileList, String sort, String order) {
        if (fileList.isEmpty()) {
            return;
        }
        fileList.sort(new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return switch (sort) {
                    case "name" ->
                            (order.equals("up")) ? f1.getName().compareTo(f2.getName()) : f2.getName().compareTo(f1.getName());
                    case "size" ->
                            (order.equals("up")) ? Long.compare(f1.length(), f2.length()) : Long.compare(f2.length(), f1.length());
                    case "gmtCreated" ->
                            (order.equals("up")) ? Long.compare(f1.lastModified(), f2.lastModified()) : Long.compare(f2.lastModified(), f1.lastModified());
                    default -> 0;
                };
            }
        });
    }

    /**
     * init storage
     */
    @Override
    public void init(Settings settings) {
        try {
            rootLocation = Paths.get(settings.getDownloadPath());
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException(Constants.STORAGE_INIT_ERROR, e);
        }
    }
}
