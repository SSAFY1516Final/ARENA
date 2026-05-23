package com.ssafy.arena.mapper;

import com.ssafy.arena.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    void insert(User user);

    User findById(@Param("id") Long id);

    User findByLoginId(@Param("loginId") String loginId);

    User findByNickname(@Param("nickname") String nickname);
}
