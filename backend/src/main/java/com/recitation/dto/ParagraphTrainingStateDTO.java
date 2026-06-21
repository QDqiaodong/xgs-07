package com.recitation.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParagraphTrainingStateDTO {

    private Long manuscriptId;

    private String manuscriptTitle;

    private Integer totalParagraphs = 0;

    private Integer progressPercent = 0;

    private Integer currentParagraphIndex = 0;

    private List<ParagraphInfo> paragraphs = new ArrayList<>();

    private List<Integer> completedParagraphs = new ArrayList<>();

    private List<Integer> strengthenParagraphs = new ArrayList<>();

    private List<Integer> skipParagraphs = new ArrayList<>();

    private Boolean trainingCompleted = false;

    @Data
    public static class ParagraphInfo {

        private Integer paragraphIndex;

        private String content;

        private String readingTip;

        private String practiceFocus;

        private String status;
    }
}
