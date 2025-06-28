package com.example.HttpDownloadServer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.example.HttpDownloadServer.dao")
public class HttpDownloadServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HttpDownloadServerApplication.class, args);
    }
}
