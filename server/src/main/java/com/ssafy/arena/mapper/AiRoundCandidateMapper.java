package com.ssafy.arena.mapper;

import com.ssafy.arena.domain.AiRoundCandidate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiRoundCandidateMapper {
    void insert(AiRoundCandidate candidate);

    List<AiRoundCandidate> findByRunId(@Param("runId") Long runId);
}
