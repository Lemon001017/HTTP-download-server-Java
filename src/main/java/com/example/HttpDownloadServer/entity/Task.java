package com.example.HttpDownloadServer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Long size;
    @TableField("url")
    private String url;
    @TableField("save_path")
    private String savePath;
    @TableField("status")
    private String status;
    @TableField("threads")
    private Integer threads;
    @TableField("speed")
    private Double speed;
    @TableField("progress")
    private Double progress;
    @TableField("remaining_time")
    private Double remainingTime;
    @TableField("chunk_num")
    private Integer chunkNum;
    @TableField("chunk_size")
    private Integer chunkSize;
}
