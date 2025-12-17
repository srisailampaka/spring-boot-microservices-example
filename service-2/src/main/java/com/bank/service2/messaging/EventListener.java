package com.bank.service2.messaging;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class EventListener {

    private final AtomicReference<String> lastMessage = new AtomicReference<>("<none>");

    @JmsListener(destination = "service.events")
    public void onMessage(String message) {
        lastMessage.set(message);
        System.out.println("[service-2] Received JMS: " + message);
    }

    public String getLastMessage() {
        return lastMessage.get();
    }
}
