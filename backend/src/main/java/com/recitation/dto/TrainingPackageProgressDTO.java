package com.recitation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TrainingPackageProgressDTO {

    private Long id;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "训练包ID不能为空")
    private Long packageId;

    private String status;

    private LocalDate startDate;

    private LocalDate completeDate;

    private Integer currentManuscriptIndex;

    private Integer totalPracticeMinutes;

    private Integer totalPracticeCount;

    private Double completionPercent;

    private Integer completedManuscripts;

    private Integer totalManuscripts;
}
