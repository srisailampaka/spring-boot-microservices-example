package com.bank.service2.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign client for calling Service-1 endpoints.
 * Automatically handles service discovery and load balancing via Eureka.
 */
@FeignClient(name = "service-1")
public interface Service1FeignClient {

    @GetMapping("/service-1/details")
    String getDetails();

    @GetMapping("/service-1/hello")
    String hello();
}
