package com.ssafy.arena.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PromptTemplateLoaderTest {
    private final PromptTemplateLoader loader = new PromptTemplateLoader();

    @Test
    void rendersTemplateWithDoubleBracePlaceholders() {
        String rendered = loader.render("Hello {{ name }}, mode={{mode}}", Map.of(
                "name", "ARENA",
                "mode", "PRACTICAL"
        ));

        assertThat(rendered).isEqualTo("Hello ARENA, mode=PRACTICAL");
    }

    @Test
    void missingPlaceholderValueFailsFast() {
        assertThatThrownBy(() -> loader.render("topic={{topic}} mode={{mode}}", Map.of("topic", "lunch")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mode");
    }
}
