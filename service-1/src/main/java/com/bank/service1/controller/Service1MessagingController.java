package com.bank.service1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service-1")
public class Service1MessagingController {

    @Autowired
    private JmsTemplate jmsTemplate;

    @GetMapping("/send-event")
    public String sendEvent(@RequestParam(name = "msg", defaultValue = "Hello from Service-1") String msg) {
        jmsTemplate.convertAndSend("service.events", msg);
        return "Sent to queue 'service.events': " + msg;
    }
}
