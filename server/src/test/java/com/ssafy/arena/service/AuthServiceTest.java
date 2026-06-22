package com.ssafy.arena.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.arena.domain.User;
import com.ssafy.arena.domain.UserRole;
import com.ssafy.arena.dto.user.KakaoLoginRequest;
import com.ssafy.arena.dto.user.KakaoUserProfile;
import com.ssafy.arena.dto.user.TokenResponse;
import com.ssafy.arena.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private KakaoOAuthClient kakaoOAuthClient;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void kakaoLoginReturnsBearerTokenForProviderUser() {
        KakaoLoginRequest request = new KakaoLoginRequest("auth-code", "http://localhost:5173/auth/kakao/callback");
        KakaoUserProfile profile = new KakaoUserProfile("12345", "카카오사용자");
        User user = User.builder()
                .id(2L)
                .loginId("kakao_12345")
                .role(UserRole.USER)
                .provider("KAKAO")
                .providerId("12345")
                .build();
        when(kakaoOAuthClient.fetchProfile(request.code(), request.redirectUri())).thenReturn(profile);
        when(userService.findOrCreateKakaoUser(profile)).thenReturn(user);
        when(jwtTokenProvider.createToken(2L, "kakao_12345", UserRole.USER)).thenReturn("kakao-jwt-token");

        TokenResponse response = authService.kakaoLogin(request);

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.accessToken()).isEqualTo("kakao-jwt-token");
        verify(userService).findOrCreateKakaoUser(profile);
    }
}
