package com.bank.service1.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewaySignatureFilter implements Filter {

    private final byte[] hmacSecret;
    private static final long ALLOWED_SKEW_MS = 120_000; // 2 minutes

    public GatewaySignatureFilter(
            @Value("${gateway.hmac.secret:changeit-gw-hmac-shared-secret-0123456789}") String secret) {
        this.hmacSecret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        if (!path.startsWith("/service-1")) {
            chain.doFilter(request, response);
            return;
        }

        String user = req.getHeader("X-Auth-User");
        String ts = req.getHeader("X-Auth-Ts");
        String sig = req.getHeader("X-Auth-Signature");

        if (ts == null || sig == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        long now = System.currentTimeMillis();
        long tsLong;
        try {
            tsLong = Long.parseLong(ts);
        } catch (NumberFormatException ex) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (Math.abs(now - tsLong) > ALLOWED_SKEW_MS) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String data = (user == null ? "" : user) + ":" + ts;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret, "HmacSHA256"));
            String expected = Base64.getEncoder()
                    .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
            if (!constantTimeEquals(expected, sig)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception ex) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
