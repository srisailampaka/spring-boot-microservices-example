package com.bank.service1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/service-1")
public class Service1ReactiveController {

    @Autowired
    private WebClient webClient;

    @GetMapping("/service2-reactive-details")
    public Mono<String> getService2ReactiveDetails() {
        return webClient
                .get()
                .uri("http://service-2/service-2/details")
                .retrieve()
                .bodyToMono(String.class)
                .map(b -> "Service-2 via reactive call: " + b);
    }
}
