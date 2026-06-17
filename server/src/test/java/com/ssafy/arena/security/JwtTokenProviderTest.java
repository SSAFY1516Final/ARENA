package com.ssafy.arena.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.arena.config.JwtProperties;
import com.ssafy.arena.domain.UserRole;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {
    @Test
    void tokenContainsUserRoleClaim() {
        JwtTokenProvider provider = new JwtTokenProvider(
                new JwtProperties("test-secret-key-that-is-long-enough-32bytes", 3600)
        );

        String token = provider.createToken(1L, "admin", UserRole.ADMIN);

        assertThat(provider.getLoginId(token)).isEqualTo("admin");
        assertThat(provider.getRole(token)).isEqualTo(UserRole.ADMIN);
    }
}
