package com.recitation.service.difficulty;

import com.recitation.dto.DifficultyAssessmentDTO;
import com.recitation.enums.DifficultyLevel;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 文稿难度评估器：依据 {@link DifficultyRuleLibrary} 规则库，
 * 结合篇幅、句式复杂度、情绪跨度三个维度对文稿内容进行评估并给出推荐难度。
 */
@Service
public class DifficultyAssessor {

    private static final Pattern SENTENCE_SPLIT_PATTERN = Pattern.compile("[。！？!?；;\\n]+");

    private static final int LONG_SENTENCE_THRESHOLD = 40;

    @Resource
    private DifficultyRuleLibrary ruleLibrary;

    public DifficultyAssessmentDTO assess(String content) {
        DifficultyMetrics metrics = computeMetrics(content);

        DifficultyLevel lengthLevel = ruleLibrary.levelForLength(metrics.getCharCount());
        DifficultyLevel complexityLevel = ruleLibrary.levelForComplexity(metrics.getAvgSentenceLength());
        DifficultyLevel emotionLevel = ruleLibrary.levelForEmotion(metrics.getEmotionCategoryCount());

        List<DifficultyAssessmentDTO.DimensionResult> dimensions = new ArrayList<>();
        dimensions.add(buildDimension(
                DifficultyRuleLibrary.DIMENSION_LENGTH,
                (double) metrics.getCharCount(),
                metrics.getCharCount() + " 字",
                lengthLevel,
                ruleLibrary.describeLength(lengthLevel)));
        dimensions.add(buildDimension(
                DifficultyRuleLibrary.DIMENSION_COMPLEXITY,
                metrics.getAvgSentenceLength(),
                String.format("%.1f 字/句", metrics.getAvgSentenceLength()),
                complexityLevel,
                ruleLibrary.describeComplexity(complexityLevel)));
        dimensions.add(buildDimension(
                DifficultyRuleLibrary.DIMENSION_EMOTION,
                (double) metrics.getEmotionCategoryCount(),
                metrics.getEmotionCategoryCount() + " 类情绪"
                        + (metrics.getMatchedEmotionCategories().isEmpty()
                        ? "" : "（" + String.join("、", metrics.getMatchedEmotionCategories()) + "）"),
                emotionLevel,
                ruleLibrary.describeEmotion(emotionLevel)));

        double overallScore = round2(
                lengthLevel.getLevel() * ruleLibrary.getWeight(DifficultyRuleLibrary.DIMENSION_LENGTH)
                        + complexityLevel.getLevel() * ruleLibrary.getWeight(DifficultyRuleLibrary.DIMENSION_COMPLEXITY)
                        + emotionLevel.getLevel() * ruleLibrary.getWeight(DifficultyRuleLibrary.DIMENSION_EMOTION)
        );
        DifficultyLevel recommended = levelFromScore(overallScore);

        DifficultyAssessmentDTO dto = new DifficultyAssessmentDTO();
        dto.setRecommendedLevel(recommended.getLabel());
        dto.setRecommendedLevelNum(recommended.getLevel());
        dto.setOverallScore(overallScore);
        dto.setDimensions(dimensions);
        return dto;
    }

    private DifficultyAssessmentDTO.DimensionResult buildDimension(String dimension,
                                                                    double metricValue,
                                                                    String metricLabel,
                                                                    DifficultyLevel level,
                                                                    String description) {
        DifficultyAssessmentDTO.DimensionResult result = new DifficultyAssessmentDTO.DimensionResult();
        result.setDimension(dimension);
        result.setDimensionLabel(ruleLibrary.getDimensionLabel(dimension));
        result.setMetricValue(round2(metricValue));
        result.setMetricLabel(metricLabel);
        result.setLevel(level.getLabel());
        result.setLevelNum(level.getLevel());
        result.setWeight(ruleLibrary.getWeight(dimension));
        result.setWeightedScore(round2(level.getLevel() * ruleLibrary.getWeight(dimension)));
        result.setDescription(description);
        return result;
    }

    DifficultyMetrics computeMetrics(String content) {
        DifficultyMetrics metrics = new DifficultyMetrics();
        if (content == null || content.trim().isEmpty()) {
            metrics.setAvgSentenceLength(0d);
            metrics.setLongSentenceRatio(0d);
            metrics.setMatchedEmotionCategories(new LinkedHashSet<>());
            return metrics;
        }

        String normalized = content.trim();
        metrics.setCharCount(countNonWhitespace(normalized));

        List<String> sentences = splitSentences(normalized);
        metrics.setSentenceCount(sentences.size());
        if (sentences.isEmpty()) {
            metrics.setAvgSentenceLength(0d);
            metrics.setLongSentenceCount(0);
            metrics.setLongSentenceRatio(0d);
        } else {
            int totalLength = 0;
            int longCount = 0;
            for (String sentence : sentences) {
                int len = sentence.length();
                totalLength += len;
                if (len > LONG_SENTENCE_THRESHOLD) {
                    longCount++;
                }
            }
            metrics.setAvgSentenceLength(round2((double) totalLength / sentences.size()));
            metrics.setLongSentenceCount(longCount);
            metrics.setLongSentenceRatio(round2((double) longCount / sentences.size()));
        }

        Set<String> matched = detectEmotionCategories(normalized);
        metrics.setMatchedEmotionCategories(matched);
        metrics.setEmotionCategoryCount(matched.size());

        return metrics;
    }

    private int countNonWhitespace(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    private List<String> splitSentences(String content) {
        String[] parts = SENTENCE_SPLIT_PATTERN.split(content);
        List<String> sentences = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                sentences.add(trimmed);
            }
        }
        return sentences;
    }

    private Set<String> detectEmotionCategories(String content) {
        Set<String> matched = new LinkedHashSet<>();
        for (Map.Entry<String, List<String>> entry : ruleLibrary.getEmotionDictionary().entrySet()) {
            for (String keyword : entry.getValue()) {
                if (content.contains(keyword)) {
                    matched.add(entry.getKey());
                    break;
                }
            }
        }
        return matched;
    }

    private DifficultyLevel levelFromScore(double score) {
        if (score < 1.75) {
            return DifficultyLevel.BEGINNER;
        } else if (score < 2.75) {
            return DifficultyLevel.PRIMARY;
        } else if (score < 3.5) {
            return DifficultyLevel.INTERMEDIATE;
        }
        return DifficultyLevel.ADVANCED;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
