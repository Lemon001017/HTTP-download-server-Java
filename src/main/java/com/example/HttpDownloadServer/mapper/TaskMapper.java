package com.example.HttpDownloadServer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.HttpDownloadServer.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    @Select("SELECT * FROM task WHERE status = #{status}")
    List<Task> getTasksByStatus(@Param("status") String status);
}
