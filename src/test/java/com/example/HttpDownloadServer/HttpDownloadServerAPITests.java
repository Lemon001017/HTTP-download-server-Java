package com.example.HttpDownloadServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class HttpDownloadServerAPITests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetSettings() {

    }

    @Test
    public void testUpdateSettings() {

    }

    @Test
    public void testGetTaskList() {

    }

    @Test
    public void testDeleteTasks() {

    }

    @Test
    public void testSubmit() {

    }

    @Test
    public void testPause() {

    }

    @Test
    public void testResume() {

    }

    @Test
    public void testRestart() {

    }

    @Test
    public void testGetFileList() {
    }
}
