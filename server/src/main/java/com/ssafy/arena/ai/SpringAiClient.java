package com.ssafy.arena.ai;

import com.ssafy.arena.common.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiClient {
    private final ChatClient.Builder chatClientBuilder;

    public String call(String systemPrompt, String userPrompt) {
        try {
            return chatClientBuilder.build()
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (RuntimeException ex) {
            log.error("Spring AI provider call failed", ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Spring AI provider call failed");
        }
    }
}
