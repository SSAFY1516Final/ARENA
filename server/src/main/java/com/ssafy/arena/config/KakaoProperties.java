package com.ssafy.arena.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arena.kakao")
public record KakaoProperties(
        String restApiKey,
        String clientSecret,
        String redirectUri
) {
}
