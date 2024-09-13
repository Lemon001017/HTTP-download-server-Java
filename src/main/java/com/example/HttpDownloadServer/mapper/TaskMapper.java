package com.example.HttpDownloadServer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.HttpDownloadServer.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
