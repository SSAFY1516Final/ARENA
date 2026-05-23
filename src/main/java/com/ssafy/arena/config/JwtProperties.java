package com.ssafy.arena.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arena.jwt")
public record JwtProperties(
        String secret,
        long expirationSeconds
) {
}
