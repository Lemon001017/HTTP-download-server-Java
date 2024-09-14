package com.example.HttpDownloadServer.constant;

public final class Constants {
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

}
