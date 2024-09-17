package com.example.HttpDownloadServer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Settings;
import com.example.HttpDownloadServer.mapper.SettingsMapper;
import com.example.HttpDownloadServer.service.FileService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.sql.Wrapper;


@SpringBootApplication
@MapperScan("com.example.HttpDownloadServer.mapper")
public class HttpDownloadServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HttpDownloadServerApplication.class, args);
    }

    @Bean
    CommandLineRunner initDataBase(SettingsMapper settingsMapper, FileService fileService) {
        return args -> {
            Settings settings=new Settings(1, Constants.DEFAULT_DOWNLOAD_ROOT_PATH,Constants.DEFAULT_MAX_TASKS,Constants.DEFAULT_MAX_DOWNLOAD_SPEED);
            if(settingsMapper.selectById(1)!=null){
                settingsMapper.updateById(settings);
            }else {
                settingsMapper.insert(settings);
            }
            fileService.init(settings);

        };
    }
}
