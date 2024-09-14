package com.example.HttpDownloadServer.exception;

import com.example.HttpDownloadServer.config.HTTPStatusConfig;
import com.example.HttpDownloadServer.param.HTTPStatusParam;
import org.springframework.web.server.ResponseStatusException;

/**
 * 封装异常信息
 **/
public class DownloadException extends ResponseStatusException {
        /**
         * 构造函数
         * @param param 异常封装参数
         **/
        public DownloadException(HTTPStatusParam param) {

            super(param.httpStatus,param.statusMessage);
        }

        /**
         * 构造函数
         * @param param 异常封装参数
         * @param cause 异常报错
         **/
        public DownloadException(HTTPStatusParam param, Throwable cause) {
            super(param.httpStatus,param.statusMessage);
        }

    }

