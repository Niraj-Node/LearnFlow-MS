package com.lms.apigateway.filter;

import com.lms.apigateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Map;
import java.util.Optional;

@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final JwtUtil jwtUtil;

    public JwtValidationGatewayFilterFactory(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String jwt = extractJwtFromCookie(exchange);

            if (jwt == null) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            Optional<Map<String, String>> claimsOptional = jwtUtil.validateAndExtract(jwt);
            if (claimsOptional.isEmpty()) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            Map<String, String> claims = claimsOptional.get();
            String userId = claims.get("userId");
            String role = claims.get("role");

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-Role", role)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
            return chain.filter(mutatedExchange);
        };
    }

    private String extractJwtFromCookie(ServerWebExchange exchange) {
        MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
        if (cookies.containsKey("jwt")) {
            HttpCookie jwtCookie = cookies.getFirst("jwt");
            if (jwtCookie != null) {
                return jwtCookie.getValue();
            }
        }
        return null;
    }
}
