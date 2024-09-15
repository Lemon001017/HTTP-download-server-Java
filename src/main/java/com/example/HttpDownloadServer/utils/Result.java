package com.example.HttpDownloadServer.utils;

import java.io.Serializable;

public class Result<D> implements Serializable {
    private String code;
    private String message;
    private D data;

    public static <T> Result<T> create() {
        return new Result<T>();
    }

    public static Result<?> buildSuccess() {
        Result<?> result = new Result<>();
        return result;
    }

    public String getCode() {
        return code;
    }

    public Result<D> setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Result<D> setMessage(String message) {
        this.message = message;
        return this;
    }

    public D getData() {
        return data;
    }

    public Result<D> setData(D data) {
        this.data = data;
        return this;
    }
}
