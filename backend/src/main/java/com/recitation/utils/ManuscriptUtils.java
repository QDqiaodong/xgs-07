package com.recitation.utils;

import com.recitation.entity.Manuscript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManuscriptUtils {

    private static final List<String> POETRY_KEYWORDS = Arrays.asList(
            "诗", "词", "曲", "赋", "古诗", "唐诗", "宋词", "元曲", "绝句", "律诗", "乐府"
    );

    private static final String USER_ID_PREFIX = "user_";

    public static String formatUserId(String userId) {
        if (userId == null || userId.isBlank()) return null;
        String trimmed = userId.trim();
        if (trimmed.startsWith(USER_ID_PREFIX)) return trimmed;
        return USER_ID_PREFIX + trimmed;
    }

    public static String formatUserId(Long userId) {
        if (userId == null) return null;
        return USER_ID_PREFIX + userId;
    }

    public static boolean canAccessManuscript(Manuscript manuscript, String userId) {
        if (manuscript == null) return false;
        if (Boolean.TRUE.equals(manuscript.getIsPublic())) return true;
        String createUser = manuscript.getCreateUser();
        String formattedUserId = formatUserId(userId);
        return createUser != null && formattedUserId != null && createUser.equals(formattedUserId);
    }

    public static boolean canModifyManuscript(Manuscript manuscript, String userId) {
        if (manuscript == null) return false;
        String createUser = manuscript.getCreateUser();
        String formattedUserId = formatUserId(userId);
        return createUser != null && formattedUserId != null && createUser.equals(formattedUserId);
    }

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
        return splitParagraphContents(content, categoryName).size();
    }

    public static List<String> splitParagraphContents(String content, String categoryName) {
        List<String> paragraphs = new ArrayList<>();
        if (content == null || content.trim().isEmpty()) {
            return paragraphs;
        }

        String manuscriptType = detectManuscriptType(categoryName, content);
        String[] lines = content.split("\\n+");
        List<String> filteredLines = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().length() > 0) {
                filteredLines.add(line.trim());
            }
        }

        if ("poetry".equals(manuscriptType)) {
            List<String> mergedLines = new ArrayList<>();
            for (String line : filteredLines) {
                if (isHeading(line)) {
                    if (!mergedLines.isEmpty()) {
                        paragraphs.add(String.join("\n", mergedLines));
                        mergedLines = new ArrayList<>();
                    }
                } else {
                    mergedLines.add(line);
                }
            }
            if (!mergedLines.isEmpty()) {
                paragraphs.add(String.join("\n", mergedLines));
            }
        } else {
            for (String line : filteredLines) {
                if (!isHeading(line)) {
                    paragraphs.add(line);
                }
            }
        }

        return paragraphs;
    }
}
