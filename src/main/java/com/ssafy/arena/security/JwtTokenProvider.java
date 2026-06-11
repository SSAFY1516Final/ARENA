package com.ssafy.arena.security;

import com.ssafy.arena.config.JwtProperties;
import com.ssafy.arena.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Long userId, String loginId, UserRole role) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.expirationSeconds());
        return Jwts.builder()
                .subject(loginId)
                .claim("userId", userId)
                .claim("role", (role == null ? UserRole.USER : role).name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public String getLoginId(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token) {
        claims(token);
        return true;
    }

    public UserRole getRole(String token) {
        return UserRole.valueOf(claims(token).get("role", String.class));
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
