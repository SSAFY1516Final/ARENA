package com.ssafy.arena.service;

import com.ssafy.arena.domain.User;
import com.ssafy.arena.dto.user.KakaoLoginRequest;
import com.ssafy.arena.dto.user.KakaoUserProfile;
import com.ssafy.arena.dto.user.TokenResponse;
import com.ssafy.arena.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserService userService;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse kakaoLogin(KakaoLoginRequest request) {
        KakaoUserProfile profile = kakaoOAuthClient.fetchProfile(request.code(), request.redirectUri());
        User user = userService.findOrCreateKakaoUser(profile);
        return createTokenResponse(user);
    }

    private TokenResponse createTokenResponse(User user) {
        return TokenResponse.bearer(jwtTokenProvider.createToken(user.getId(), user.getLoginId(), user.getRole()));
    }
}
