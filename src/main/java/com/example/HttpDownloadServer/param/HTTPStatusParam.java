package com.example.HttpDownloadServer.param;

import org.springframework.http.HttpStatus;

/**
 * HTTP status code parameter class
 **/
public class HTTPStatusParam {
    /**
     * Status code
     **/
    public int code;
    /**
     * HTTP Status
     */
    public HttpStatus httpStatus;
    /**
     * Status message
     **/
    public String message;

    public HTTPStatusParam(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
