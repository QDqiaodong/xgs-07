package com.recitation.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AuthorProfileDTO {

    private String author;

    private int totalManuscripts;

    private int totalViews;

    private int totalFavorites;

    private List<GenreItem> genreDistribution;

    private List<DifficultyItem> difficultyDistribution;

    private List<ExpressionItem> expressionDistribution;

    private List<ManuscriptSummary> manuscripts;

    @Data
    public static class GenreItem {
        private String name;
        private int count;
        private double percentage;

        public GenreItem(String name, int count, double percentage) {
            this.name = name;
            this.count = count;
            this.percentage = Math.round(percentage * 10.0) / 10.0;
        }
    }

    @Data
    public static class DifficultyItem {
        private String name;
        private int level;
        private int count;
        private double percentage;

        public DifficultyItem(String name, int level, int count, double percentage) {
            this.name = name;
            this.level = level;
            this.count = count;
            this.percentage = Math.round(percentage * 10.0) / 10.0;
        }
    }

    @Data
    public static class ExpressionItem {
        private String type;
        private String label;
        private int count;
        private double percentage;

        public ExpressionItem(String type, String label, int count, double percentage) {
            this.type = type;
            this.label = label;
            this.count = count;
            this.percentage = Math.round(percentage * 10.0) / 10.0;
        }
    }

    @Data
    public static class ManuscriptSummary {
        private Long id;
        private String title;
        private String categoryName;
        private String difficulty;
        private Integer viewCount;
        private Integer favoriteCount;
    }
}
