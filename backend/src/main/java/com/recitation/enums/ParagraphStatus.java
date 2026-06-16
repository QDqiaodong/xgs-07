package com.recitation.enums;

import java.util.Arrays;
import java.util.List;

public enum ParagraphStatus {

    MASTERED("mastered", "已熟练"),
    STRENGTHEN("strengthen", "需加强"),
    SKIP("skip", "暂不练");

    private final String value;
    private final String label;

    ParagraphStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static final List<String> VALID_VALUES = Arrays.asList(
            MASTERED.getValue(),
            STRENGTHEN.getValue(),
            SKIP.getValue()
    );

    public static boolean isValid(String value) {
        return value != null && VALID_VALUES.contains(value);
    }
}
