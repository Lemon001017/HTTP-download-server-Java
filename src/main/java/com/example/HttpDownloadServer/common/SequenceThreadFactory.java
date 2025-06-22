package com.example.HttpDownloadServer.common;

import lombok.Getter;
import lombok.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class SequenceThreadFactory implements ThreadFactory {
    private final AtomicLong index = new AtomicLong(0);
    @Getter
    private final String name;

    public SequenceThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(String.format("%s-%d", name, index.getAndIncrement()));
        return thread;
    }

    public long getIndex() {
        return index.get();
    }
}
