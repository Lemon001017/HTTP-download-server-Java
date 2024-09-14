package com.example.HttpDownloadServer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class File {
    private String id;
    private String name;
    private String path;
    private boolean isDir;
    private long size;
    private Date gmtCreated;
    private Date gmtModified;
}
