package com.example.HttpDownloadServer.constant;

import com.example.HttpDownloadServer.param.HTTPStatusParam;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public final class Constants {
    // Default Settings
    public static final String DEFAULT_DOWNLOAD_PATH = "D:\\";
    public static final Double DEFAULT_MAX_DOWNLOAD_SPEED = 1000D;
    public static final Integer DEFAULT_MAX_TASKS = 4;
    public static final Integer DEFAULT_THREADS = 4;

    // Chunk Size
    public static final int MIN_CHUNK_SIZE = 32 * 1024;
    public static final int MID_CHUNK_SIZE = 1024 * 1024;
    public static final int MAX_CHUNK_SIZE = 10 * 1024 * 1024;

    // Task Status
    public static final String TASK_STATUS_DOWNLOADING = "downloading";
    public static final String TASK_STATUS_DOWNLOADED = "downloaded";
    public static final String TASK_STATUS_PENDING = "pending";
    public static final String TASK_STATUS_CANCELED = "canceled";
    public static final String TASK_STATUS_FAILED = "failed";

    // HTTP Status
    public static final HTTPStatusParam DOWNLOAD_TIMEOUT = new HTTPStatusParam(504, HttpStatus.GATEWAY_TIMEOUT, "download time out");
    public static final HTTPStatusParam RESOURCE_NOTFOUND = new HTTPStatusParam(503, HttpStatus.SERVICE_UNAVAILABLE, "The URL resource cannot be accessed");
    public static final HTTPStatusParam DOWNLOAD_SERVER_TERMINATION = new HTTPStatusParam(501, HttpStatus.NOT_IMPLEMENTED, "Termination of download service");

    // HTTP code
    public static final String HTTP_STATUS_OK = "200";
    public static final String HTTP_STATUS_BAD_REQUEST = "400";
    public static final String HTTP_STATUS_NOT_FOUND = "404";
    public static final String HTTP_STATUS_SERVER_ERROR = "500";

    // Settings Error
    public static final String ERR_SAVE_SETTINGS = "Failed to save settings";
    public static final String ERR_GET_SETTINGS = "Failed to get settings";

    // Task Errror
    public static final String ERR_SUBMIT_TASK = "Failed to submit task";
}
