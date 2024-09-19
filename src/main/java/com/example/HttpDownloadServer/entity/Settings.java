package com.example.HttpDownloadServer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("settings")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Settings {
    @TableId("id")
    private Integer id;

    @TableField("download_path")
    private String downloadPath;

    @TableField("max_tasks")
    private int maxTasks;

    @TableField("max_download_speed")
    private double maxDownloadSpeed;
}
