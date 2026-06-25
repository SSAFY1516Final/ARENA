package com.ssafy.arena.ai;

public final class TopicSideLabelNormalizer {
    private TopicSideLabelNormalizer() {
    }

    public static String normalize(String label) {
        if (label == null) {
            return null;
        }
        String normalized = label
                .replaceAll("[,。.!?！？]+$", "")
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.isEmpty()) {
            return normalized;
        }

        normalized = normalized
                .replaceAll("(이|가)\\s*(이긴다|승리한다|우세하다)$", "")
                .replaceAll("(이|가)\\s*더\\s*(좋다|강하다|적합하다|낫다|유리하다)$", "")
                .replaceAll("(을|를)\\s*(선택해야\\s*한다|골라야\\s*한다|선택한다|고른다)$", "")
                .replaceAll("\\s*(이긴다|승리한다|우세하다)$", "")
                .replaceAll("\\s*더\\s*(좋다|강하다|적합하다|낫다|유리하다)$", "")
                .trim();

        return normalized.isEmpty() ? label.trim() : normalized;
    }
}
