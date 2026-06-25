package com.ssafy.arena.ai;

import com.ssafy.arena.domain.Speaker;
import com.ssafy.arena.dto.ai.AiNextTurnRequest;
import org.springframework.stereotype.Component;

@Component
public class AiPromptFactory {
    public String nextTurnSystemPrompt() {
        return """
                ARENA 토론 서비스의 다음 발화를 생성한다.
                반드시 JSON만 반환한다. keys: content(string), peakReached(boolean).
                발화자는 주제의 양쪽 선택지 중 한 진영을 맡아 말한다.
                COOL_HEADED는 주제의 앞쪽 선택지, PASSIONATE는 주제의 뒤쪽 선택지를 대변한다.
                화면에는 내부 발화자명이 아니라 실제 선택지 이름이 표시된다.
                """;
    }

    public String nextTurnUserPrompt(AiNextTurnRequest request, Speaker speaker) {
        return """
                주제: %s
                모드: %s
                발화자: %s
                라운드: %d
                이전 발화: %s
                """.formatted(request.topic(), request.mode(), speaker, request.roundNo(), request.previousMessages());
    }

}
