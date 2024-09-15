package com.example.HttpDownloadServer.exception;

import com.example.HttpDownloadServer.param.HTTPStatusParam;
import org.springframework.web.server.ResponseStatusException;

/**
 * Encapsulation exception message
 **/
public class DownloadException extends ResponseStatusException {
    /**
     * constructor
     *
     * @param param
     **/
    public DownloadException(HTTPStatusParam param) {
        super(param.httpStatus, param.message);
    }

    /**
     * constructor
     *
     * @param param
     * @param cause
     **/
    public DownloadException(HTTPStatusParam param, Throwable cause) {
        super(param.httpStatus, param.message);
    }

}

