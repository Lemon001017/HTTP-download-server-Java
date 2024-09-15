package com.example.HttpDownloadServer.controller;

import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.service.SettingsService;
import com.example.HttpDownloadServer.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    @Autowired
    private SettingsService settingsService;

    /**
     * @param null
     * @return settings
     */
    @GetMapping("")
    public Result<Settings> getSettings() {
        return settingsService.getSettings();
    }

    /**
     * @param settings object
     * @return settings
     */

    @PostMapping("")
    public Result<Settings> saveSettings(@RequestBody Settings settings) {
        return settingsService.updateSettings(settings);
    }
}
