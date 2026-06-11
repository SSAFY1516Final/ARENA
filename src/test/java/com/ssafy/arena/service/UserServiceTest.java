package com.ssafy.arena.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.arena.common.ApiException;
import com.ssafy.arena.domain.User;
import com.ssafy.arena.domain.UserRole;
import com.ssafy.arena.dto.user.SignupRequest;
import com.ssafy.arena.mapper.UserMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void signupAssignsUserRoleByDefault() {
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        userService.signup(new SignupRequest("user01", "일반사용자", "password123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void listUsersReturnsAllUsers() {
        when(userMapper.findAll()).thenReturn(List.of(
                User.builder().id(1L).loginId("admin").nickname("관리자").role(UserRole.ADMIN).build(),
                User.builder().id(2L).loginId("user").nickname("사용자").role(UserRole.USER).build()
        ));

        assertThat(userService.listUsers())
                .extracting(User::getRole)
                .containsExactly(UserRole.ADMIN, UserRole.USER);
    }

    @Test
    void updateRoleRejectsSelfDemotion() {
        assertThatThrownBy(() -> userService.updateRole(1L, 1L, UserRole.USER))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateRoleUpdatesExistingUser() {
        when(userMapper.findById(2L)).thenReturn(User.builder().id(2L).role(UserRole.USER).build());

        User updated = userService.updateRole(1L, 2L, UserRole.ADMIN);

        verify(userMapper).updateRole(2L, UserRole.ADMIN);
        assertThat(updated.getRole()).isEqualTo(UserRole.ADMIN);
    }
}
