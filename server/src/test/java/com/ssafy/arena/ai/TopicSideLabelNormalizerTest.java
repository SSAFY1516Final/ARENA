package com.ssafy.arena.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TopicSideLabelNormalizerTest {
    @Test
    void removesKoreanWinningAndChoicePredicatesFromSideLabels() {
        assertThat(TopicSideLabelNormalizer.normalize("오타니 10명이 이긴다")).isEqualTo("오타니 10명");
        assertThat(TopicSideLabelNormalizer.normalize("북극곰이 이긴다")).isEqualTo("북극곰");
        assertThat(TopicSideLabelNormalizer.normalize("JPA가 더 적합하다")).isEqualTo("JPA");
        assertThat(TopicSideLabelNormalizer.normalize("MyBatis를 선택해야 한다")).isEqualTo("MyBatis");
    }

    @Test
    void keepsAlreadyShortNounLabels() {
        assertThat(TopicSideLabelNormalizer.normalize("오타니 10명")).isEqualTo("오타니 10명");
        assertThat(TopicSideLabelNormalizer.normalize("북극곰")).isEqualTo("북극곰");
    }
}
