package com.example.HttpDownloadServer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class File {
    private String name;
    private String path;
    private long size;
    private Date gmtModified;
}
