package com.example.HttpDownloadServer;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HttpDownloadServerAPITemplateTests {
    @Autowired
    private TestRestTemplate restTemplate;
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

    @Test
    public void testFetchFileList() throws Exception {
//        System.out.println(restTemplate); // 输出 restTemplate 对象
//        if (restTemplate == null) {
//            throw new IllegalStateException("restTemplate is null!");
//        }
//        FileParams fileParams = new FileParams("All", "name", "up");
//        ResponseEntity<String> response = this.restTemplate.getForEntity("/api/fileList", String.class, fileParams);
//        assertThat(response.getStatusCode()).isEqualTo(200);

    }
}
