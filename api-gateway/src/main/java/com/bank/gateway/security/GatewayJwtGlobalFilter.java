package com.bank.gateway.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class GatewayJwtGlobalFilter implements GlobalFilter, Ordered {

    private final Key key;
    private final byte[] hmacSecret;

    public GatewayJwtGlobalFilter(
            @Value("${jwt.secret:changeit-changeit-changeit-changeit-0123456789}") String secret,
            @Value("${gateway.hmac.secret:changeit-gw-hmac-shared-secret-0123456789}") String hmacSecret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.hmacSecret = hmacSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/service-1") || path.startsWith("/service-2")) {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            String token = authHeader.substring(7);
            try {
                var claimsJws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
                var subject = claimsJws.getBody().getSubject();
                var ts = String.valueOf(System.currentTimeMillis());
                var data = (subject == null ? "" : subject) + ":" + ts;
                var mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(hmacSecret, "HmacSHA256"));
                var sig = Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));

                var mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-Auth-User", subject != null ? subject : "")
                    .header("X-Auth-Ts", ts)
                    .header("X-Auth-Signature", sig)
                    .header("X-From-Gateway", "true")
                    .build();

                var mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                return chain.filter(mutatedExchange);
            } catch (Exception ex) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
