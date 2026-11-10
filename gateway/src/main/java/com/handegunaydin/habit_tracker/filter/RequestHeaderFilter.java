package com.handegunaydin.habit_tracker.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestHeaderFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("RequestHeaderFilter çalıştı");
        ServerWebExchange mutatedExchange  = exchange.mutate().request(exchange
                .getRequest()
                .mutate()
                .headers(header -> {
                    header.remove("X-User-Id");
                    header.remove("X-User-Roles");
                }).build()).build();

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .map(auth -> ((JwtAuthenticationToken) auth).getToken())
                .map(token -> withUserId(exchange,token))
                .switchIfEmpty(Mono.fromSupplier(() -> withUserId(exchange, null)))
                .flatMap(chain::filter);
    }

    private ServerWebExchange withUserId(ServerWebExchange exchange, Jwt token) {
        if(token.getSubject() == null) return exchange;
        String roles = token.getClaimAsString("roles");

        return exchange.mutate().request(r -> {
            r.header("X-User-Id", token.getSubject());
            if(!roles.isEmpty()) {
                r.header("X-User-Roles", roles);
            }
        }).build();

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
