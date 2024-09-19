package com.example.HttpDownloadServer.param;

import com.example.HttpDownloadServer.entity.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResFileParams {
    public List<File> fileList;
    public int total;
}
