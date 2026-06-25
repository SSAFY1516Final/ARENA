package com.ssafy.arena.dto.user;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class UpdateNicknameRequestTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsKoreanNickname() {
        var violations = validator.validate(new UpdateNicknameRequest("테스트닉네임"));

        assertThat(violations).isEmpty();
    }

    @Test
    void acceptsEnglishNumbersAndUnderscoreNickname() {
        var violations = validator.validate(new UpdateNicknameRequest("arena_user1"));

        assertThat(violations).isEmpty();
    }
}
