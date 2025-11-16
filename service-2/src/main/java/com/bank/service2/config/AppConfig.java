package com.bank.service2.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Application configuration class for Service-2.
 * Enables Feign clients for inter-service communication.
 */
@Configuration
@EnableFeignClients(basePackages = "com.bank.service2.client")
public class AppConfig {


	@Bean
	@LoadBalanced
	@Primary
	public WebClient.Builder loadBalancedWebClientBuilder() {
		return WebClient.builder();
	}

	/**
	 * WebClient bean for reactive HTTP calls. Use with Reactor types (Mono/Flux).
	 */
	@Bean
	public WebClient webClient(@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder builder) {
		return builder.build();
	}

}
