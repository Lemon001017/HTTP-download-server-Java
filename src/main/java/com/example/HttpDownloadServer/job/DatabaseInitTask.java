package com.example.HttpDownloadServer.job;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.dao.SettingsMapper;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database initialization service
 * Automatically creates required tables on application startup
 */
@Slf4j
@Component
public class DatabaseInitTask implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private SettingsMapper settingsMapper;

    @Autowired
    private FileService fileService;

    private static final String SETTINGS_TABLE = "settings";
    private static final String TASK_TABLE = "task";

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting database initialization...");
        
        try {
            List<String> missingTables = missingTables();
            
            if (!missingTables.isEmpty()) {
                log.info("Missing tables detected: {}", missingTables);
                createMissingTables(missingTables);
                log.info("Database initialization completed successfully");
            } else {
                log.info("All required tables already exist");
            }
            
            // Initialize default settings if settings table is empty
            initializeDefaultSettings();
            
        } catch (Exception e) {
            log.error("Database initialization failed", e);
            throw e;
        }
    }

    /**
     * Check which required tables are missing
     */
    private List<String> missingTables() throws SQLException {
        List<String> missingTables = new ArrayList<>();
        List<String> requiredTables = List.of(SETTINGS_TABLE, TASK_TABLE);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();

            for (String tableName : requiredTables) {
                try (ResultSet tables = metaData.getTables(catalog, schema, tableName, new String[]{"TABLE"})) {
                    if (!tables.next()) {
                        missingTables.add(tableName);
                        log.info("Table '{}' not found, will be created", tableName);
                    }
                }
            }
        }

        return missingTables;
    }

    /**
     * Create missing tables using SQL scripts
     */
    private void createMissingTables(List<String> missingTables) throws IOException {
        for (String tableName : missingTables) {
            String sqlScript = loadSqlScript(tableName);
            if (sqlScript != null) {
                try {
                    // Split SQL script by semicolon and execute each statement
                    String[] statements = sqlScript.split(";");
                    for (String statement : statements) {
                        String trimmedStatement = statement.trim();
                        if (!trimmedStatement.isEmpty()) {
                            jdbcTemplate.execute(trimmedStatement);
                            log.debug("Executed SQL: {}", trimmedStatement);
                        }
                    }
                    log.info("Successfully created table: {}", tableName);
                } catch (Exception e) {
                    log.error("Failed to create table: {}", tableName, e);
                    throw new RuntimeException("Failed to create table: " + tableName, e);
                }
            } else {
                log.error("SQL script not found for table: {}", tableName);
                throw new RuntimeException("SQL script not found for table: " + tableName);
            }
        }
    }

    /**
     * Initialize default settings if settings table is empty
     */
    private void initializeDefaultSettings() {
        try {
            // Check if settings table has any data using MyBatis-Plus
            List<Settings> existingSettings = settingsMapper.selectList(null);
            
            Settings settings;
            if (existingSettings == null || existingSettings.isEmpty()) {
                log.info("Settings table is empty, inserting default settings");
                
                // Create default settings
                settings = new Settings();
                settings.setId(1);
                settings.setDownloadPath(Constants.DEFAULT_DOWNLOAD_ROOT_PATH);
                settings.setMaxDownloadSpeed(Constants.DEFAULT_MAX_DOWNLOAD_SPEED);
                settings.setMaxTasks(Constants.DEFAULT_MAX_TASKS);
                
                // Insert default settings using MyBatis-Plus
                settingsMapper.insert(settings);
                
                log.info("Default settings inserted successfully: download_path={}, max_download_speed={}, max_tasks={}", 
                    settings.getDownloadPath(), 
                    settings.getMaxDownloadSpeed(), 
                    settings.getMaxTasks());
            } else {
                log.info("Settings table already has data, using existing settings");
                settings = existingSettings.get(0);
            }
            
            // Initialize FileService with settings
            fileService.init(settings);
            log.info("FileService initialized successfully with download path: {}", settings.getDownloadPath());
            
        } catch (Exception e) {
            log.error("Failed to initialize default settings", e);
            throw new RuntimeException("Failed to initialize default settings", e);
        }
    }

    /**
     * Load SQL script from resources
     */
    private String loadSqlScript(String tableName) throws IOException {
        String scriptPath = "/" + tableName + ".sql";
        try {
            ClassPathResource resource = new ClassPathResource(scriptPath);
            if (resource.exists()) {
                return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            } else {
                log.warn("SQL script not found at path: {}", scriptPath);
                return null;
            }
        } catch (IOException e) {
            log.error("Failed to load SQL script: {}", scriptPath, e);
            throw e;
        }
    }
} 