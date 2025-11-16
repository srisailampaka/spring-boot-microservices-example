package com.bank.service2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.service2.client.Service1FeignClient;



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
@RequestMapping("/service-2")
public class Service2Controller {

  @Autowired
  private Service1FeignClient service1FeignClient;

   @RequestMapping("/hello")
    public String hello() {
        return "Hello from Service 2";
    }

    @RequestMapping("/details")
    public String getDetails() {
        return "Details from Service 2";
    }

 
   
    @RequestMapping("/service1-details")
    public String getService1Details() {
        try {
            // Call service-1's /details endpoint using Feign client
            String response = service1FeignClient.getDetails();
            return "Service-1 Details: " + response;
        } catch (Exception e) {
            return "Error calling service-1: " + e.getMessage();
        }
    }
}