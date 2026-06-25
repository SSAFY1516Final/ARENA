package com.ssafy.arena.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserPrincipalTest {
    @Test
    void exposesAuthorityFromUserRole() {
        User admin = User.builder()
                .id(1L)
                .loginId("admin")
                .nickname("관리자")
                .passwordHash("hash")
                .role(UserRole.ADMIN)
                .build();

        UserPrincipal principal = new UserPrincipal(admin);

        assertThat(principal.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(principal.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_ADMIN");
    }
}
