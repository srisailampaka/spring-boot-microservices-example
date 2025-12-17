package com.bank.service2.controller;

import com.bank.service2.messaging.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service-2")
public class Service2MessageController {

    @Autowired
    private EventListener eventListener;

    @GetMapping("/last-event")
    public String lastEvent() {
        return "Last JMS event: " + eventListener.getLastMessage();
    }
}
