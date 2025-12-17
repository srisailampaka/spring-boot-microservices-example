================================================================================
  SPRING BOOT MICROSERVICES WITH NETFLIX EUREKA - PROJECT OVERVIEW
================================================================================

PROJECT: Spring Boot Microservices Example with Service Registry
REPOSITORY: spring-boot-microservices-example
OWNER: srisailampaka
TECH STACK: Spring Boot 3.0.13, Spring Cloud 2022.0.5, Java 17, Maven 3.9.11

================================================================================
  ARCHITECTURE OVERVIEW
================================================================================

This project demonstrates a complete microservices architecture with:
- Service Discovery (Netflix Eureka)
- API Gateway (Spring Cloud Gateway)
- JWT-based Authentication & Authorization
- Inter-service Communication (REST, Feign, WebClient)
- Message Broker Integration (ActiveMQ Artemis)
- HTTPS/TLS Security
- Load Balancing (Spring Cloud LoadBalancer)

================================================================================
  SERVICES & PORTS
================================================================================

1. EUREKA SERVICE REGISTRY (Port: 8761)
   - Service discovery server
   - Dashboard: https://localhost:8761/
   - All services register here for discovery
   - HTTPS enabled with self-signed certificate

2. API GATEWAY (Port: 8080)
   - Entry point for all client requests
   - JWT validation via GlobalFilter
   - Routes: /service-1/**, /service-2/**, /auth/**
   - Injects X-Auth-User and X-Auth-Signature headers
   - Load balances requests across service instances

3. AUTH-SERVICE (Port: 8090)
   - Authentication and authorization service
   - HTTPS enabled with self-signed certificate
   - Endpoints:
     * POST /auth/register - Register new user
     * POST /auth/login - Login and get JWT token
     * GET /auth/validate - Validate JWT token
     * GET /admin/users - List all users (secured)
   - User store: H2 in-memory database
   - H2 Console: https://localhost:8090/h2-console
   - JWT signing with HS256 algorithm

4. SERVICE-1 (Port: 8084)
   - Business service with REST endpoints
   - Endpoints:
     * GET /service-1/hello - Returns greeting with authenticated user
     * GET /service-1/details - Returns service details
     * GET /service-1/service2-reactive-details - Calls service-2 via WebClient
     * GET /service-1/send-event?msg=xxx - Sends JMS message to Artemis
   - Gateway signature verification enabled
   - Load-balanced RestTemplate and WebClient configured
   - JMS producer for ActiveMQ Artemis

5. SERVICE-2 (Port: 8085)
   - Business service with REST and reactive endpoints
   - Endpoints:
     * GET /service-2/hello - Returns greeting
     * GET /service-2/details - Returns service details
     * GET /service-2/service1-details - Calls service-1 via Feign
     * GET /service-2/reactive-details - Calls service-1 via WebClient
     * GET /service-2/stream - Server-Sent Events stream
     * GET /service-2/last-event - Returns last JMS message received
   - OpenFeign client for service-1
   - Load-balanced WebClient for reactive calls
   - JMS consumer listening on queue "service.events"

================================================================================
  SECURITY FEATURES
================================================================================

1. JWT AUTHENTICATION
   - Issued by auth-service after login
   - Validated at API Gateway before routing
   - Subject extracted and forwarded as X-Auth-User header
   - 1 hour expiration time

2. GATEWAY HMAC SIGNATURE
   - Gateway signs requests with HMAC-SHA256
   - Signature: Base64(HMAC-SHA256(user:timestamp, secret))
   - Service-1 verifies signature to prevent gateway bypass
   - 2-minute timestamp skew tolerance

3. HTTPS/TLS
   - Eureka: Self-signed certificate (eureka-keystore.p12)
   - Auth-service: Self-signed certificate (auth-keystore.p12)
   - Clients use truststore (eureka-truststore.jks)
   - TrustStore configured via JVM arguments in pom.xml

4. SPRING SECURITY
   - Auth-service: Stateless session, JWT filter, BCrypt passwords
   - Public endpoints: /auth/login, /auth/register, /auth/validate
   - Protected endpoints: /admin/** requires authentication

================================================================================
  MESSAGING (ACTIVEMQ ARTEMIS)
================================================================================

BROKER CONFIGURATION:
- URL: tcp://localhost:61616
- Username: admin
- Password: admin
- Queue: service.events

FLOW:
1. Client → API Gateway → service-1 /send-event?msg=xxx
2. service-1 sends message to Artemis queue
3. service-2 JMS listener consumes message
4. Client → API Gateway → service-2 /last-event to retrieve

NOTE: Requires ActiveMQ Artemis running locally or via Docker:
  docker run -d --name artemis -p 61616:61616 -p 8161:8161 vromero/activemq-artemis

================================================================================
  INTER-SERVICE COMMUNICATION
================================================================================

1. OPENFEIGN (Declarative REST Client)
   - service-2 → service-1 via Service1FeignClient
   - Load balanced via @FeignClient(name="service-1")

2. RESTTEMPLATE (Synchronous REST)
   - @LoadBalanced RestTemplate in service-1
   - Call by service ID: http://service-2/service-2/hello

3. WEBCLIENT (Reactive REST)
   - @LoadBalanced WebClient.Builder in service-1 and service-2
   - Returns Mono/Flux for reactive streams
   - Example: webClient.get().uri("http://service-1/...").retrieve().bodyToMono(String.class)

4. JMS MESSAGING (Asynchronous)
   - service-1 produces to queue
   - service-2 consumes via @JmsListener

================================================================================
  CONFIGURATION FILES
================================================================================

API-GATEWAY (application.yml):
- spring.cloud.gateway.routes
- eureka.client.serviceUrl.defaultZone=https://localhost:8761/eureka/
- jwt.secret
- gateway.hmac.secret

SERVICE-1 (application.properties):
- server.port=8084
- eureka.client.serviceUrl.defaultZone=https://localhost:8761/eureka/
- gateway.hmac.secret
- spring.artemis.broker-url=tcp://localhost:61616

SERVICE-2 (application.properties):
- server.port=8085
- eureka.client.serviceUrl.defaultZone=https://localhost:8761/eureka/
- spring.artemis.broker-url=tcp://localhost:61616

AUTH-SERVICE (application.properties):
- server.port=8090
- server.ssl.* (HTTPS configuration)
- jwt.secret
- spring.datasource.* (H2 configuration)

EUREKA (application.properties):
- server.port=8761
- server.ssl.* (HTTPS configuration)
- eureka.client.register-with-eureka=false

================================================================================
  RUNNING THE PROJECT
================================================================================

PREREQUISITES:
- Java 17 or higher
- Maven 3.8+
- ActiveMQ Artemis (optional, for messaging demo)

STARTUP SEQUENCE:

Option 1: Use the startup script
  .\start-all-services.ps1

Option 2: Manual startup (run in order)
  1. Start Eureka:
     mvn -f eureka-service-registry\pom.xml spring-boot:run

  2. Wait 30 seconds, then start auth-service:
     mvn -f auth-service\pom.xml spring-boot:run

  3. Start service-1:
     mvn -f service-1\pom.xml spring-boot:run

  4. Start service-2:
     mvn -f service-2\pom.xml spring-boot:run

  5. Start API Gateway:
     mvn -f api-gateway\pom.xml spring-boot:run

VERIFY STARTUP:
- Eureka Dashboard: https://localhost:8761/
- Check all services are registered and UP

================================================================================
  TESTING THE APIS
================================================================================

STEP 1: REGISTER USER (PowerShell)
  [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
  [System.Net.ServicePointManager]::ServerCertificateValidationCallback = { $true }
  $body = @{ username='testuser'; password='testpass' } | ConvertTo-Json
  Invoke-RestMethod -Method Post -Uri https://localhost:8090/auth/register -Body $body -ContentType 'application/json'

STEP 2: LOGIN & GET JWT TOKEN
  $login = Invoke-RestMethod -Method Post -Uri https://localhost:8090/auth/login -Body $body -ContentType 'application/json'
  $token = $login.token
  $h = @{ Authorization = "Bearer $token" }

STEP 3: TEST ENDPOINTS VIA GATEWAY
  # Service-1 endpoints
  Invoke-RestMethod -Uri http://localhost:8080/service-1/hello -Headers $h
  Invoke-RestMethod -Uri http://localhost:8080/service-1/details -Headers $h
  Invoke-RestMethod -Uri http://localhost:8080/service-1/service2-reactive-details -Headers $h

  # Service-2 endpoints
  Invoke-RestMethod -Uri http://localhost:8080/service-2/hello -Headers $h
  Invoke-RestMethod -Uri http://localhost:8080/service-2/details -Headers $h
  Invoke-RestMethod -Uri http://localhost:8080/service-2/service1-details -Headers $h
  Invoke-RestMethod -Uri http://localhost:8080/service-2/reactive-details -Headers $h

  # SSE Stream (4 second sample)
  curl.exe -H "Authorization: Bearer $token" http://localhost:8080/service-2/stream --max-time 4

STEP 4: TEST MESSAGING (requires Artemis)
  # Send message
  Invoke-RestMethod -Uri "http://localhost:8080/service-1/send-event?msg=HelloWorld" -Headers $h

  # Check received message
  Invoke-RestMethod -Uri http://localhost:8080/service-2/last-event -Headers $h

================================================================================
  POSTMAN TESTING
================================================================================

1. LOGIN:
   Method: POST
   URL: https://localhost:8090/auth/login
   Body (raw JSON): { "username": "testuser", "password": "testpass" }
   Accept self-signed cert warning
   Copy the "token" from response

2. CALL SERVICE VIA GATEWAY:
   Method: GET
   URL: http://localhost:8080/service-1/hello
   Headers: Authorization: Bearer <paste-token-here>

================================================================================
  PROJECT STRUCTURE
================================================================================

spring-boot-microservices-netflix-eureka-service-registry-example/
├── api-gateway/
│   ├── src/main/java/com/bank/gateway/
│   │   ├── GatewayApplication.java
│   │   └── security/
│   │       ├── GatewayJwtGlobalFilter.java (JWT validation & HMAC signing)
│   │       ├── GatewayJwtFilter.java
│   │       └── GatewaySecurityConfig.java
│   └── src/main/resources/
│       └── application.yml
├── eureka-service-registry/
│   ├── src/main/java/com/bank/EurekaServiceRegistryApplication.java
│   └── src/main/resources/
│       ├── application.properties
│       └── eureka-keystore.p12
├── auth-service/
│   ├── src/main/java/com/bank/auth/
│   │   ├── AuthServiceApplication.java
│   │   ├── controller/ (AuthController, AdminController)
│   │   ├── model/ (UserEntity)
│   │   ├── repository/ (UserRepository)
│   │   ├── security/ (SecurityConfig, JwtUtil, JwtFilter)
│   │   └── service/ (UserService)
│   └── src/main/resources/
│       ├── application.properties
│       └── auth-keystore.p12
├── service-1/
│   ├── src/main/java/com/bank/service1/
│   │   ├── Service1Application.java
│   │   ├── config/ (AppConfig - LoadBalancer beans)
│   │   ├── controller/ (Service1Controller, Service1ReactiveController, Service1MessagingController)
│   │   └── security/ (GatewaySignatureFilter)
│   └── src/main/resources/
│       └── application.properties
├── service-2/
│   ├── src/main/java/com/bank/service2/
│   │   ├── Service2Application.java
│   │   ├── client/ (Service1FeignClient)
│   │   ├── config/ (AppConfig - Feign & LoadBalancer)
│   │   ├── controller/ (Service2Controller, Service2ReactiveController, Service2MessageController)
│   │   └── messaging/ (EventListener)
│   └── src/main/resources/
│       └── application.properties
├── java-version-comparison/
│   ├── java8-example/ (Java 8 feature demonstrations)
│   └── java21-example/ (Java 21 feature demonstrations)
├── eureka-truststore.jks (Client truststore for HTTPS Eureka)
├── start-all-services.ps1 (PowerShell startup script)
└── README.txt (This file)

================================================================================
  SPRING CLOUD FEATURES DEMONSTRATED
================================================================================

✓ Service Discovery & Registration (Netflix Eureka)
✓ Client-Side Load Balancing (Spring Cloud LoadBalancer)
✓ API Gateway with Routing (Spring Cloud Gateway)
✓ Declarative REST Client (OpenFeign)
✓ Circuit Breaker Ready (dependencies in place for Resilience4j)
✓ Configuration Management (application.yml/properties)
✓ Distributed Tracing Ready (Micrometer compatible)

================================================================================
  ADDITIONAL FEATURES
================================================================================

✓ JWT-based stateless authentication
✓ HMAC request signing for gateway bypass prevention
✓ HTTPS with self-signed certificates
✓ Reactive programming with WebFlux
✓ Server-Sent Events (SSE) streaming
✓ JMS messaging with ActiveMQ Artemis
✓ Spring Security 6 integration
✓ H2 in-memory database with JPA
✓ BCrypt password encoding
✓ Pattern matching and records (Java 21 comparison module)

================================================================================
  TROUBLESHOOTING
================================================================================

ISSUE: Services can't connect to Eureka
FIX: Ensure Eureka is running and accessible at https://localhost:8761/
     Check truststore configuration in pom.xml

ISSUE: JWT validation fails (401 Unauthorized)
FIX: Register user via /auth/register first
     Login via /auth/login to get fresh token
     Token expires in 1 hour

ISSUE: SSL/Certificate errors
FIX: Add certificate validation bypass in PowerShell:
     [System.Net.ServicePointManager]::ServerCertificateValidationCallback = { $true }

ISSUE: JMS send-event fails (500 error)
FIX: Start ActiveMQ Artemis broker on tcp://localhost:61616
     Or remove spring-boot-starter-artemis dependency temporarily

ISSUE: Port already in use
FIX: Kill process using the port:
     netstat -ano | findstr :<port>
     taskkill /PID <pid> /F

================================================================================
  SECRETS & CREDENTIALS
================================================================================

⚠️  DEFAULT CREDENTIALS (Change in production!)

JWT Secret: changeit-changeit-changeit-changeit-0123456789
Gateway HMAC Secret: changeit-gw-hmac-shared-secret-0123456789
Keystore Password: changeit
Artemis Username: admin
Artemis Password: admin

================================================================================
  PRODUCTION CONSIDERATIONS
================================================================================

Before deploying to production:
1. Replace self-signed certificates with CA-signed certificates
2. Move secrets to environment variables or secret management system (Vault)
3. Enable actual circuit breakers with Resilience4j
4. Add distributed tracing (Zipkin/Jaeger)
5. Configure centralized logging (ELK stack)
6. Set up monitoring (Prometheus + Grafana)
7. Use external configuration server (Spring Cloud Config)
8. Implement proper database with connection pooling
9. Add rate limiting and throttling at gateway
10. Configure proper CORS policies
11. Use container orchestration (Kubernetes/Docker Swarm)
12. Implement health checks and readiness probes

================================================================================
  LICENSE & CONTACT
================================================================================

Repository: https://github.com/srisailampaka/spring-boot-microservices-example
Owner: srisailampaka
Created: 2025

For questions or issues, please create an issue on the GitHub repository.

================================================================================
  END OF DOCUMENTATION
================================================================================
