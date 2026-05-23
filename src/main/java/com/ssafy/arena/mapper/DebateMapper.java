package com.ssafy.arena.mapper;

import com.ssafy.arena.domain.*;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DebateMapper {
    void insertSession(DebateSession session);

    DebateSession findSessionById(@Param("id") Long id);

    void updateSessionStatus(@Param("id") Long id, @Param("status") DebateStatus status);

    void updatePeakReached(@Param("id") Long id, @Param("peakReached") boolean peakReached);

    void insertMessage(DebateMessage message);

    List<DebateMessage> findMessages(@Param("debateSessionId") Long debateSessionId);

    Integer findNextRoundNo(@Param("debateSessionId") Long debateSessionId);

    void insertSummary(DebateSummary summary);

    DebateSummary findSummary(@Param("debateSessionId") Long debateSessionId);
}
