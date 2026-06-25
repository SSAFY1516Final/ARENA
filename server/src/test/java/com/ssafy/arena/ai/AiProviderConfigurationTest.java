package com.ssafy.arena.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class AiProviderConfigurationTest {
    @Test
    @SuppressWarnings("unchecked")
    void applicationDefaultsPointSpringAiOpenAiClientToGmsProxy() {
        Map<String, Object> root;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.yml")) {
            root = new Yaml().load(inputStream);
        } catch (Exception ex) {
            throw new AssertionError("application.yml should be readable", ex);
        }

        Map<String, Object> spring = (Map<String, Object>) root.get("spring");
        Map<String, Object> ai = (Map<String, Object>) spring.get("ai");
        Map<String, Object> openai = (Map<String, Object>) ai.get("openai");
        Map<String, Object> chat = (Map<String, Object>) openai.get("chat");
        Map<String, Object> options = (Map<String, Object>) chat.get("options");

        assertThat(openai.get("api-key")).isEqualTo("${GMS_KEY:}");
        assertThat(openai.get("base-url")).isEqualTo("${GMS_BASE_URL:https://gms.ssafy.io/gmsapi/api.openai.com}");
        assertThat(chat.get("completions-path")).isEqualTo("${GMS_COMPLETIONS_PATH:/v1/chat/completions}");
        assertThat(options.get("model")).isEqualTo("${GMS_MODEL:gpt-5.4-mini}");
    }
}
