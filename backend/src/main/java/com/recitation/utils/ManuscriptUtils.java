package com.recitation.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManuscriptUtils {

    private static final List<String> POETRY_KEYWORDS = Arrays.asList(
            "诗", "词", "曲", "赋", "古诗", "唐诗", "宋词", "元曲", "绝句", "律诗", "乐府"
    );

    public static String detectManuscriptType(String categoryName, String content) {
        if (categoryName == null) categoryName = "";
        if (content == null) content = "";

        for (String kw : POETRY_KEYWORDS) {
            if (categoryName.contains(kw)) {
                return "poetry";
            }
        }

        String[] lines = content.split("\\n+");
        List<String> filteredLines = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().length() > 0) {
                filteredLines.add(line.trim());
            }
        }

        int shortLineCount = 0;
        for (String line : filteredLines) {
            if (line.length() <= 12) {
                shortLineCount++;
            }
        }

        boolean isPoetryContent = filteredLines.size() >= 4 &&
                (shortLineCount * 1.0 / filteredLines.size()) >= 0.6;

        return isPoetryContent ? "poetry" : "prose";
    }

    private static boolean isHeading(String trimmed) {
        if (trimmed.length() >= 15) return false;
        if (trimmed.endsWith("：") || trimmed.endsWith(":")) return true;
        return trimmed.matches("^[第零一二三四五六七八九十\\d]+.*");
    }

    public static int countParagraphs(String content, String categoryName) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }

        String manuscriptType = detectManuscriptType(categoryName, content);
        String[] lines = content.split("\\n+");
        List<String> filteredLines = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().length() > 0) {
                filteredLines.add(line.trim());
            }
        }

        int paragraphCount = 0;

        if ("poetry".equals(manuscriptType)) {
            int mergedLines = 0;
            for (String line : filteredLines) {
                if (isHeading(line)) {
                    if (mergedLines > 0) {
                        paragraphCount++;
                        mergedLines = 0;
                    }
                } else {
                    mergedLines++;
                }
            }
            if (mergedLines > 0) {
                paragraphCount++;
            }
        } else {
            for (String line : filteredLines) {
                if (!isHeading(line)) {
                    paragraphCount++;
                }
            }
        }

        return paragraphCount;
    }
}
