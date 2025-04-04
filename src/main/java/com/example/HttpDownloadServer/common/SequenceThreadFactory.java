package com.example.HttpDownloadServer.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class SequenceThreadFactory implements ThreadFactory {
    private final AtomicLong index = new AtomicLong(0);
    private final String name;

    public SequenceThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(String.format("%s-%d", name, index.getAndIncrement()));
        return thread;
    }

    public String getName() {
        return name;
    }

    public long getIndex() {
        return index.get();
    }
}
