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

@Service
public class SseServiceImpl implements SseService {

    private static final Logger log = LoggerFactory.getLogger(SseServiceImpl.class);

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();


    @Override
    public Result<SseEmitter> subscribe(String id) {
        Result<SseEmitter> result = new Result<>();
        SseEmitter sseEmitter = new SseEmitter(60 * 1000L);
        emitters.put(id, sseEmitter);
        sseEmitter.onCompletion(() -> emitters.remove(id));
        sseEmitter.onTimeout(() -> emitters.remove(id));
        result.setCode(Constants.HTTP_STATUS_OK);
        result.setData(sseEmitter);
        return result;
    }

    @Override
    public void send(String id, Task task) {
        SseEmitter sseEmitter = emitters.get(id);
        if (sseEmitter != null) {
            try {
                sseEmitter.send(task);
            } catch (IOException e) {
                log.error("Failed to send SSE message to client: {} id:{}", e.getMessage(), id);
                emitters.remove(id);
            }
        }
    }
}
