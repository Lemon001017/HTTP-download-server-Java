package com.example.HttpDownloadServer.service;

import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.utils.Result;

public interface SettingsService {
    Result<Settings> updateSettings(Settings settings);

    Result<Settings> getSettings();
}
