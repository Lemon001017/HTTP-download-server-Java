package com.example.HttpDownloadServer.service.impl;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver {
    @RabbitListener(queues = "exampleQueue")
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
    }
}