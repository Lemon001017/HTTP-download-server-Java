package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.dao.SettingsMapper;
import com.example.HttpDownloadServer.service.SettingsService;
import com.example.HttpDownloadServer.param.Result;
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
        try {
            settingsMapper.updateById(settings);
            result.setData(settings);
            result.setCode(Constants.HTTP_STATUS_OK);
            result.setMessage("Settings updated successfully");
            log.info("Update settings success");
        } catch (Exception e) {
            log.error("Failed to update settings", e);
            result.setCode(Constants.HTTP_STATUS_SERVER_ERROR);
            result.setMessage(Constants.ERR_SAVE_SETTINGS);
        }
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
