package com.recitation.dto;

import lombok.Data;

import java.util.List;

@Data
public class PracticeCalendarDTO {

    private String date;

    private Integer manuscriptCount;

    private Integer totalPracticeCount;

    private Integer estimatedMinutes;

    private String keyPoints;

    private List<ManuscriptPracticeInfo> manuscripts;

    @Data
    public static class ManuscriptPracticeInfo {
        private Long manuscriptId;
        private String manuscriptTitle;
        private Integer practiceCount;
        private Integer emotionScore;
        private String noteSummary;
    }
}
