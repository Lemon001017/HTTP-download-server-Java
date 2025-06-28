package com.example.HttpDownloadServer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
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
@Service
public class DatabaseInitService implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SETTINGS_TABLE = "settings";
    private static final String TASK_TABLE = "task";

    @Override
    public void run(String... args) throws Exception {
        try {
            List<String> missingTables = missingTables();
            if (!missingTables.isEmpty()) {
                createMissingTables(missingTables);
                log.info("Database initialization completed successfully");
            }
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