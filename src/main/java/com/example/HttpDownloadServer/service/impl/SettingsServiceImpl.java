package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.mapper.SettingsMapper;
import com.example.HttpDownloadServer.service.SettingsService;
import com.example.HttpDownloadServer.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsServiceImpl implements SettingsService {
    @Autowired
    private SettingsMapper settingsMapper;
    private static final Logger log = LoggerFactory.getLogger(SettingsServiceImpl.class);

    @Override
    public Result<Settings> updateSettings(Settings settings) {
        Result<Settings> result = new Result<>();
        if (settings == null) {
            result.setCode(Constants.HTTP_STATUS_BAD_REQUEST);
            result.setMessage(Constants.ERR_SAVE_SETTINGS);
            log.error("Save settings error");
            return result;
        }
        if (settings.getDownloadPath() == null) {
            settings.setDownloadPath(Constants.DEFAULT_DOWNLOAD_ROOT_PATH);
        }
        if (settings.getMaxDownloadSpeed() <= 0) {
            settings.setMaxDownloadSpeed(Constants.DEFAULT_MAX_DOWNLOAD_SPEED);
        }
        if (settings.getMaxTasks() <= 0) {
            settings.setMaxTasks(Constants.DEFAULT_MAX_TASKS);
        }

        Settings settings1 = settingsMapper.selectById(1);
        if (settings1 == null) {
            settings.setId(1);
            settingsMapper.insert(settings);
        } else {
            settingsMapper.updateById(settings);
        }
        result.setData(settings);
        result.setCode(Constants.HTTP_STATUS_OK);
        log.info("Update settings success");
        return result;
    }

    @Override
    public Result<Settings> getSettings() {
        Result<Settings> result = new Result<>();
        Settings settings = settingsMapper.selectOne(null);
        if (settings == null) {
            result.setCode(Constants.HTTP_STATUS_SERVER_ERROR);
            result.setMessage(Constants.ERR_GET_SETTINGS);
            log.error("Get settings error");
            return result;
        }
        result.setData(settings);
        result.setCode(Constants.HTTP_STATUS_OK);
        log.info("Get settings success");
        return result;
    }
}
