package com.example.HttpDownloadServer.exception;

import com.example.HttpDownloadServer.param.HTTPStatusParam;
import org.springframework.web.server.ResponseStatusException;

public class StorageException extends ResponseStatusException {
    /**
     * constructor
     *
     * @param param
     **/
    public StorageException(HTTPStatusParam param) {
        super(param.httpStatus, param.message);
    }

    /**
     * constructor
     *
     * @param param
     * @param cause
     **/
    public StorageException(HTTPStatusParam param, Throwable cause) {
        super(param.httpStatus, param.message);
    }

}
