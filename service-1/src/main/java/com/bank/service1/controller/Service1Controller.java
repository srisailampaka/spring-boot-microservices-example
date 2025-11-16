package com.bank.service1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * REST controller that handles requests for the "service-1" API surface.
 *
 * <p>Mapped at the base path "/service-1", this controller delegates request handling to a
 * Service1Service instance supplied via constructor injection. It exposes two simple endpoints:
 *
 * <ul>
 *   <li>GET /service-1/hello - returns a short greeting or status string produced by the service.</li>
 *   <li>GET /service-1/details - returns a details string produced by the service.</li>
 * </ul>
 *
 * <p>Implementation notes:
 * <ul>
 *   <li>Uses constructor-based dependency injection to obtain the required Service1Service.</li>
 *   <li>Methods return plain String responses; content negotiation, error handling, and response
 *       formatting can be handled centrally (e.g., by Spring MVC configuration or global exception handlers).</li>
 *   <li>The controller itself is stateless; ensure the injected service is thread-safe if it is shared.</li>
 * </ul>
 *
 * @see com.bank.service1.Service1Service
 * @since 1.0
 */
@RestController
@RequestMapping("/service-1")
public class Service1Controller {

    @RequestMapping("/hello")
    public String hello(@org.springframework.web.bind.annotation.RequestHeader(value = "X-Auth-User", required = false) String user) {
        String u = (user == null || user.isBlank()) ? "anonymous" : user;
        return "Hello from Service 1 (user=" + u + ")";
    }

    @RequestMapping("/details")
    public String getDetails() {
        return "Details from Service 1";
    }
}
