package com.ssafy.arena.controller;

import com.ssafy.arena.domain.User;
import com.ssafy.arena.dto.user.*;
import com.ssafy.arena.security.JwtTokenProvider;
import com.ssafy.arena.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest request) {
        return UserResponse.from(userService.signup(request));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticate(request);
        return TokenResponse.bearer(jwtTokenProvider.createToken(user.getId(), user.getLoginId(), user.getRole()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        // JWT is stateless. Clients complete logout by deleting the stored token.
    }
}
