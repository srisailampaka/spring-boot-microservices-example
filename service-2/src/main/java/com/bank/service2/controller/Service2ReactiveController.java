package com.bank.service2.controller;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Reactive endpoints demonstrating Mono and Flux usage.
 */
@RestController
@RequestMapping("/service-2")
public class Service2ReactiveController {

    @Autowired
    private WebClient webClient;

    @GetMapping("/reactive-details")
    public Mono<String> reactiveDetails() {
        return webClient.get()
            .uri("http://service-1/service-1/details")
                .retrieve()
                .bodyToMono(String.class)
                .map(b -> "Reactive: " + b)
                .onErrorResume(e -> Mono.just("Error: " + e.getMessage()));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> "tick: " + i)
                .take(10);
    }
}
