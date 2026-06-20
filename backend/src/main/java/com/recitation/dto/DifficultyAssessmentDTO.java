package com.recitation.dto;

import lombok.Data;

import java.util.List;

/**
 * 难度评估结果：结合篇幅、句式复杂度、情绪跨度三个维度给出推荐难度。
 */
@Data
public class DifficultyAssessmentDTO {

    private String recommendedLevel;

    private Integer recommendedLevelNum;

    private Double overallScore;

    private List<DimensionResult> dimensions;

    @Data
    public static class DimensionResult {

        private String dimension;

        private String dimensionLabel;

        private Double metricValue;

        private String metricLabel;

        private String level;

        private Integer levelNum;

        private Double weight;

        private Double weightedScore;

        private String description;
    }
}
