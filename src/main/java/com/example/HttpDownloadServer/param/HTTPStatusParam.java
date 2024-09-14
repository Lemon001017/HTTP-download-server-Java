package com.example.HttpDownloadServer.param;

import org.springframework.http.HttpStatus;
/**
 * HTTP的状态码参数类
**/
public class HTTPStatusParam {
    /**
     * 状态码
     **/
    public int statusCode;
    /**
     * HTTPStatus
     */
    public HttpStatus httpStatus;
    /**
     * 状态信息
     **/
    public String statusMessage;
    HTTPStatusParam(){}
    public HTTPStatusParam(int statusCode, HttpStatus httpStatus, String statusMessage){
        this.statusCode = statusCode;
        this.httpStatus = httpStatus;
        this.statusMessage = statusMessage;
    }
}
