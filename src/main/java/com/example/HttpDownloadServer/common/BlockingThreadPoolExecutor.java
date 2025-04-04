package com.example.HttpDownloadServer.common;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {
    public BlockingThreadPoolExecutor(int poolSize, String name) {
        this(poolSize, new SequenceThreadFactory(name));
    }

    public BlockingThreadPoolExecutor(int poolSize, ThreadFactory threadFactory) {
        this(poolSize, poolSize, threadFactory);
    }

    public BlockingThreadPoolExecutor(int poolSize, int queueSize, ThreadFactory threadFactory) {
        super(poolSize, poolSize, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueSize), threadFactory, (r, executor) -> {
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
