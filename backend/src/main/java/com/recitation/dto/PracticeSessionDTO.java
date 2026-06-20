package com.recitation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PracticeSessionDTO {

    private Long id;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "文稿ID不能为空")
    private Long manuscriptId;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer durationSeconds;

    private String completedParagraphs;

    private String selfAssessmentStatus;

    private Integer selfAssessmentScore;

    private String selfAssessmentNote;
}
