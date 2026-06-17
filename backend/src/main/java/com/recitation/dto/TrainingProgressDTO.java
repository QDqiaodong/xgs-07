package com.recitation.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainingProgressDTO {

    private Long manuscriptId;

    private Integer masteredCount = 0;

    private Integer strengthenCount = 0;

    private Integer skipCount = 0;

    private Integer totalParagraphs = 0;

    private Integer progressPercent = 0;

    private Integer totalPracticeCount = 0;

    private LocalDateTime lastPracticeTime;

    private Boolean hasNote = false;

    private LocalDateTime noteUpdateTime;
}
