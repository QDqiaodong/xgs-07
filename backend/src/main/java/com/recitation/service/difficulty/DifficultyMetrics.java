package com.recitation.service.difficulty;

import lombok.Data;

import java.util.Set;

/**
 * 文本难度度量原始数据：由 {@link DifficultyAssessor} 对文稿内容计算得出。
 */
@Data
public class DifficultyMetrics {

    private int charCount;

    private double avgSentenceLength;

    private int sentenceCount;

    private int longSentenceCount;

    private double longSentenceRatio;

    private int emotionCategoryCount;

    private Set<String> matchedEmotionCategories;
}
