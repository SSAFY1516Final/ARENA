package com.ssafy.arena.mapper;

import com.ssafy.arena.domain.AiRoundCandidateRun;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiRoundCandidateRunMapper {
    void insert(AiRoundCandidateRun run);

    AiRoundCandidateRun findById(@Param("id") Long id);

    void markSucceeded(
            @Param("id") Long id,
            @Param("topicFrameJson") String topicFrameJson,
            @Param("finalResponseJson") String finalResponseJson
    );

    void markFailed(
            @Param("id") Long id,
            @Param("topicFrameJson") String topicFrameJson,
            @Param("errorMessage") String errorMessage
    );
}
