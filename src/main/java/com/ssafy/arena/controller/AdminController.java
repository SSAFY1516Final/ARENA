package com.ssafy.arena.controller;

import com.ssafy.arena.domain.UserPrincipal;
import com.ssafy.arena.dto.admin.AdminPostVisibilityResponse;
import com.ssafy.arena.dto.admin.AdminUserResponse;
import com.ssafy.arena.dto.admin.UpdatePostVisibilityRequest;
import com.ssafy.arena.dto.admin.UpdateUserRoleRequest;
import com.ssafy.arena.service.PostService;
import com.ssafy.arena.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final PostService postService;

    @GetMapping("/users")
    public List<AdminUserResponse> users() {
        return userService.listUsers().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @PatchMapping("/users/{userId}/role")
    public AdminUserResponse updateUserRole(
            @AuthenticationPrincipal UserPrincipal admin,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return AdminUserResponse.from(userService.updateRole(admin.getId(), userId, request.role()));
    }

    @PatchMapping("/posts/{postId}/visibility")
    public AdminPostVisibilityResponse updatePostVisibility(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostVisibilityRequest request
    ) {
        return AdminPostVisibilityResponse.from(postService.updateVisibility(postId, request.isPublic()));
    }
}
