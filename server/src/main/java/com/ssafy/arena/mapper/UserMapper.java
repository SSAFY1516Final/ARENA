package com.ssafy.arena.mapper;

import com.ssafy.arena.domain.User;
import com.ssafy.arena.domain.UserRole;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    void insert(User user);

    User findById(@Param("id") Long id);

    User findByLoginId(@Param("loginId") String loginId);

    User findByProvider(@Param("provider") String provider, @Param("providerId") String providerId);

    User findByNickname(@Param("nickname") String nickname);

    List<User> findAll();

    void updateNickname(@Param("id") Long id, @Param("nickname") String nickname);

    void updateRole(@Param("id") Long id, @Param("role") UserRole role);
}
