package com.example.HttpDownloadServer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("task")
public class Task {
    @TableId
    private String id;
    @TableField("name")
    private String name;
    @TableField("type")
    private String type;
    @TableField("size")
    private long size;
    @TableField("url")
    private String url;
    @TableField("save_path")
    private String savePath;
    @TableField("status")
    private String status;
    @TableField("threads")
    private int threads;
    @TableField("speed")
    private double speed;
    @TableField("progress")
    private double progress;
    @TableField("remaining_time")
    private double remainingTime;
    @TableField("chunk_num")
    private int chunkNum;
    @TableField("chunk_size")
    private int chunkSize;
}
