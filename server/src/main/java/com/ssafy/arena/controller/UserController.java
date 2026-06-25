package com.ssafy.arena.controller;

import com.ssafy.arena.domain.UserPrincipal;
import com.ssafy.arena.dto.user.UpdateNicknameRequest;
import com.ssafy.arena.dto.user.UserResponse;
import com.ssafy.arena.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal user) {
        return UserResponse.from(userService.getById(user.getId()));
    }

    @PatchMapping("/me/nickname")
    public UserResponse updateNickname(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody UpdateNicknameRequest request
    ) {
        return UserResponse.from(userService.updateNickname(user.getId(), request.nickname()));
    }
}
