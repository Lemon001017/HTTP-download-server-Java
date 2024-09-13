package com.example.HttpDownloadServer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("settings")
public class Settings {
    @TableId("id")
    private int id;
    @TableField("download_path")
    private String downloadPath;
    @TableField("max_tasks")
    private int maxTasks;
    @TableField("max_download_speed")
    private double maxDownloadSpeed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public int getMaxTasks() {
        return maxTasks;
    }

    public void setMaxTasks(int maxTasks) {
        this.maxTasks = maxTasks;
    }

    public double getMaxDownloadSpeed() {
        return maxDownloadSpeed;
    }

    public void setMaxDownloadSpeed(double maxDownloadSpeed) {
        this.maxDownloadSpeed = maxDownloadSpeed;
    }
}
