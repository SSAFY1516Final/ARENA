package com.ssafy.arena.mapper;

import com.ssafy.arena.domain.AiDebateTurnLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiDebateTurnLogMapper {
    void insert(AiDebateTurnLog log);
}
