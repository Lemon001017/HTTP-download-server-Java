package com.example.HttpDownloadServer.param;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<D> implements Serializable {
    private String code;
    private String message;
    private D data;
}
