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
        Result<List<com.example.HttpDownloadServer.entity.File >> result = new Result<>();
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
            ArrayList<File> fileList = filterFilesByType(fileStream, params.getType());
            // 自定义排序
            sortFiles(fileList, params.getSort(), params.getOrder());
            // 关闭流
            pathStream.close();
            List<com.example.HttpDownloadServer.entity.File> files = new ArrayList<>();
            fileList.forEach(otherFile -> {
                files.add(new com.example.HttpDownloadServer.entity.File(otherFile.getName(),otherFile.getPath().substring(18),otherFile.length(),new Date(otherFile.lastModified())));
            });
            result.setCode(Constants.HTTP_STATUS_OK);
            return result.setData(files);
        } catch (IOException e) {
            throw new StorageException(Constants.STORAGE_READ_ERROR, e);
        }
    }

    private ArrayList<File> filterFilesByType(Stream<File> fileList, String type) {
        return new ArrayList<>(fileList.filter(file -> switch (type) {
            case "All" -> true; // 如果类型为 All，则返回所有文件
            case "Video" -> file.getName().endsWith(".mp4") || file.getName().endsWith(".mov");
            case "Photo" ->
                    file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".gif");
            case "Archive" ->
                    file.getName().endsWith(".zip") || file.getName().endsWith(".rar") || file.getName().endsWith(".tar");
            case "Document" ->
                    file.getName().endsWith(".pptx") || file.getName().endsWith(".docx") || file.getName().endsWith(".xlsx");
            default -> false; // 默认情况下返回 false
        }).toList());
    }

    private void sortFiles(ArrayList<File> fileList, String sort, String order) {
        if (fileList.isEmpty()) {
            // 如果文件列表为空或为null，则不需要排序
            return;
        }
        Collections.sort(fileList, new Comparator<File>() {
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
     **/
    @Override
    public void init(Settings settings) {
        try {
            rootLocation=Paths.get(settings.getDownloadPath());
            // 创建下载路径文件夹,若该文件夹已经存在则不做任何操作
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException(Constants.STORAGE_INIT_ERROR, e);
        }
    }
}
