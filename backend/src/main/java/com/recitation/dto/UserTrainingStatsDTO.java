package com.recitation.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserTrainingStatsDTO {

    private Long userId;

    private Integer totalManuscriptsPracticed = 0;

    private Integer totalParagraphsMastered = 0;

    private Integer totalPracticeCount = 0;

    private List<TrainingProgressDTO> progressList;
}
