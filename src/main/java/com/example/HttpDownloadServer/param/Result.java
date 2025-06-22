package com.example.HttpDownloadServer.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Result<D> implements Serializable {
    @Serial
    private static final long serialVersionUID = 6609695320569623961L;
    private String code;
    private String message;
    private D data;
}