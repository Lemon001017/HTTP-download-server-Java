package com.example.HttpDownloadServer.service.impl;

import com.example.HttpDownloadServer.constant.Constants;
import com.example.HttpDownloadServer.entity.Task;
import com.example.HttpDownloadServer.service.SseService;
import com.example.HttpDownloadServer.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class SseServiceImpl implements SseService {

    private static final Logger log = LoggerFactory.getLogger(SseServiceImpl.class);

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public Result<SseEmitter> subscribe(String id) {
        Result<SseEmitter> result = new Result<>();

        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(30));
        emitters.put(id, emitter);

        emitter.onCompletion(() -> emitters.remove(id));
        emitter.onTimeout(() -> {
            emitters.remove(id);
            log.warn("SSE connection timed out for task id: {}", id);
        });
        emitter.onError((e) -> {
            emitters.remove(id);
            log.error("SSE connection error for task id: {}", id, e);
        });

        result.setData(emitter);
        result.setCode(Constants.HTTP_STATUS_OK); // 成功订阅
        return result;
    }

    @Override
    public void send(String id, Task task) {
        SseEmitter emitter = emitters.get(id);
        if (emitter != null) {
            try {
                emitter.send(task);
            } catch (IOException e) {
                log.error("Failed to send SSE event to task id: {}", id, e);
                emitters.remove(id);
            }
        } else {
            log.warn("No SSE emitter found for task id: {}", id);
        }
    }

    @Override
    public void close(String id) {
        SseEmitter emitter = emitters.remove(id);
        if (emitter != null) {
            emitter.complete(); // 关闭 SSE 连接
        } else {
            log.error("No SSE emitter found for task id: {}", id);
        }
    }
}
