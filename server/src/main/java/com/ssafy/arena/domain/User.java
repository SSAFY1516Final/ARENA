package com.ssafy.arena.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String loginId;
    private String nickname;
    private String passwordHash;
    private String provider;
    private String providerId;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
