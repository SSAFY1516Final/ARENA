package com.ssafy.arena.service;

import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.config.KakaoProperties;
import com.ssafy.arena.dto.user.KakaoUserProfile;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {
    private final KakaoProperties properties;
    private final RestClient restClient = RestClient.create();

    public KakaoUserProfile fetchProfile(String code, String requestRedirectUri) {
        String accessToken = exchangeAccessToken(code, requestRedirectUri);
        Map<?, ?> response = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("id") == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "failed to read Kakao profile");
        }

        Map<?, ?> properties = response.get("properties") instanceof Map<?, ?> map ? map : Map.of();
        String nickname = properties.get("nickname") instanceof String value ? value : "카카오사용자";
        return new KakaoUserProfile(String.valueOf(response.get("id")), nickname);
    }

    private String exchangeAccessToken(String code, String requestRedirectUri) {
        if (properties.restApiKey() == null || properties.restApiKey().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Kakao REST API key is not configured");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.restApiKey());
        form.add("redirect_uri", resolveRedirectUri(requestRedirectUri));
        form.add("code", code);
        if (properties.clientSecret() != null && !properties.clientSecret().isBlank()) {
            form.add("client_secret", properties.clientSecret());
        }

        Map<?, ?> response = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        if (response == null || !(response.get("access_token") instanceof String accessToken)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "failed to exchange Kakao access token");
        }
        return accessToken;
    }

    private String resolveRedirectUri(String requestRedirectUri) {
        if (requestRedirectUri != null && !requestRedirectUri.isBlank()) {
            return requestRedirectUri;
        }
        if (properties.redirectUri() != null && !properties.redirectUri().isBlank()) {
            return properties.redirectUri();
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "Kakao redirect URI is not configured");
    }
}
