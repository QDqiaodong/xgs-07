package com.recitation.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum DifficultyLevel {

    BEGINNER("入门", 1, "短句多、用词简单，适合朗读初学者"),
    PRIMARY("初级", 2, "句式常规、无明显生僻字，需一定朗读基础"),
    INTERMEDIATE("中级", 3, "含长句与绕口词，情绪有一定起伏，需朗读技巧"),
    ADVANCED("高级", 4, "长句密集、情绪跨度大、多音字多，专业级朗读挑战");

    private final String label;
    private final int level;
    private final String description;

    DifficultyLevel(String label, int level, String description) {
        this.label = label;
        this.level = level;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public static final List<String> VALID_LABELS = Arrays.asList(
            BEGINNER.label,
            PRIMARY.label,
            INTERMEDIATE.label,
            ADVANCED.label
    );

    public static boolean isValid(String label) {
        return label != null && VALID_LABELS.contains(label);
    }

    public static Optional<DifficultyLevel> fromLabel(String label) {
        if (label == null) {
            return Optional.empty();
        }
        for (DifficultyLevel value : values()) {
            if (value.label.equals(label)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
