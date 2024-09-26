package com.example.HttpDownloadServer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@TableName("task")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Task {
    @TableId
    private String id;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("size")
    private long size;

    @TableField("total_downloaded")
    private long totalDownloaded;

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

    @TableField("gmt_created")
    private LocalDateTime gmtCreated;

    public Task(String id, String name, String type, long size, String url,
                String savePath, String status, int threads, int chunkNum, int chunkSize, LocalDateTime gmtCreated) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.size = size;
        this.url = url;
        this.savePath = savePath;
        this.status = status;
        this.threads = threads;
        this.chunkNum = chunkNum;
        this.chunkSize = chunkSize;
        this.gmtCreated = gmtCreated;
    }
}
