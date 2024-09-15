package com.example.HttpDownloadServer.TestDAO;

import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.mapper.SettingsMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SettingsMapperTests {
    @Autowired
    private SettingsMapper settingsMapper;

    @Test
    public void testUpdateSettings() {
        Settings settings = new Settings(1, "test", 2, 3);
        boolean result = settingsMapper.insertOrUpdate(settings);
        assertEquals(true, result);
    }

    @Test
    public void testGetSettings() {
        Settings settings = new Settings(1, "test", 2, 3);
        assertEquals(true, settingsMapper.insertOrUpdate(settings));
        Settings result = settingsMapper.selectById(1);
        assertEquals("test", result.getDownloadPath());
        assertEquals(2, result.getMaxTasks());
        assertEquals(3, result.getMaxDownloadSpeed());
    }
}
