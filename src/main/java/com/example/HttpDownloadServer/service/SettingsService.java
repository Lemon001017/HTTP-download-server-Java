package com.example.HttpDownloadServer.service;

import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.param.Result;

public interface SettingsService {
    Result<Settings> updateSettings(Settings settings);

    Result<Settings> getSettings();
}
