package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseServiceImpl implements SseService {

    private final Map<String, Many<Task>> sinks = new ConcurrentHashMap<>();

    @Override
    public Flux<Task> subscribe(String id) {
        Many<Task> sink = Sinks.many().multicast().onBackpressureBuffer();
        sinks.put(id, sink);
        log.info("SSE subscription created for task id: {}", id);
        return sink.asFlux()
                .doOnCancel(() -> {
                    sinks.remove(id);
                    log.info("SSE subscription cancelled for task id: {}", id);
                })
                .doOnError(throwable -> {
                    sinks.remove(id);
                    log.error("SSE subscription error for task id: {}", id, throwable);
                })
                .doOnComplete(() -> {
                    sinks.remove(id);
                    log.info("SSE subscription completed for task id: {}", id);
                });
    }

    @Override
    public void send(String id, Task task) {
        Many<Task> sink = sinks.get(id);
        if (sink != null) {
            try {
                sink.tryEmitNext(task);
                log.debug("SSE event sent for task id: {}", id);
            } catch (Exception e) {
                log.error("Failed to send SSE event to task id: {}", id, e);
                sinks.remove(id);
            }
        } else {
            log.warn("No SSE sink found for task id: {}", id);
        }
    }

    @Override
    public void close(String id) {
        Many<Task> sink = sinks.remove(id);
        if (sink != null) {
            sink.tryEmitComplete();
            log.info("SSE connection closed for task id: {}", id);
        }
    }
}
