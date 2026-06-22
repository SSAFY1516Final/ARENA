package com.ssafy.arena.service;

import com.ssafy.arena.domain.User;
import com.ssafy.arena.domain.UserPrincipal;
import com.ssafy.arena.domain.UserRole;
import com.ssafy.arena.dto.user.*;
import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    private static final String LOCAL_PROVIDER = "LOCAL";
    private static final String KAKAO_PROVIDER = "KAKAO";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signup(SignupRequest request) {
        if (userMapper.findByLoginId(request.loginId()) != null) {
            throw new ApiException(HttpStatus.CONFLICT, "loginId already exists");
        }
        if (userMapper.findByNickname(request.nickname()) != null) {
            throw new ApiException(HttpStatus.CONFLICT, "nickname already exists");
        }

        User user = User.builder()
                .loginId(request.loginId())
                .nickname(request.nickname())
                .passwordHash(passwordEncoder.encode(request.password()))
                .provider(LOCAL_PROVIDER)
                .role(UserRole.USER)
                .build();
        userMapper.insert(user);
        return user;
    }

    @Transactional
    public User findOrCreateKakaoUser(KakaoUserProfile profile) {
        User user = userMapper.findByProvider(KAKAO_PROVIDER, profile.providerId());
        if (user != null) {
            return user;
        }

        User kakaoUser = User.builder()
                .loginId("kakao_" + profile.providerId())
                .nickname(resolveUniqueNickname(profile.nickname(), profile.providerId()))
                .passwordHash(null)
                .provider(KAKAO_PROVIDER)
                .providerId(profile.providerId())
                .role(UserRole.USER)
                .build();
        userMapper.insert(kakaoUser);
        return kakaoUser;
    }

    public User authenticateLocal(LoginRequest request) {
        User user = userMapper.findByLoginId(request.loginId());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "invalid login credentials");
        }
        return user;
    }

    public User getById(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "user not found");
        }
        return user;
    }

    public List<User> listUsers() {
        return userMapper.findAll();
    }

    @Transactional
    public User updateRole(Long adminUserId, Long targetUserId, UserRole role) {
        if (adminUserId.equals(targetUserId) && role == UserRole.USER) {
            throw new ApiException(HttpStatus.CONFLICT, "cannot remove your own admin role");
        }
        User user = getById(targetUserId);
        userMapper.updateRole(targetUserId, role);
        user.setRole(role);
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByLoginId(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UserPrincipal(user);
    }

    private String resolveUniqueNickname(String nickname, String providerId) {
        String baseNickname = (nickname == null || nickname.isBlank()) ? "카카오사용자" : nickname.trim();
        if (userMapper.findByNickname(baseNickname) == null) {
            return baseNickname;
        }
        String suffix = providerId.length() > 6 ? providerId.substring(providerId.length() - 6) : providerId;
        String candidate = baseNickname + "_" + suffix;
        if (candidate.length() > 50) {
            candidate = candidate.substring(0, 50);
        }
        return candidate;
    }
}
