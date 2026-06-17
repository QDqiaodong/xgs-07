package com.recitation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PracticeNoteDTO {

    private Long id;

    @NotNull(message = "文稿ID不能为空")
    private Long manuscriptId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String difficultyPoints;

    private String toneControl;

    private String emotionExpression;

    private String otherNotes;

    private Integer emotionControlScore;

    private String emotionControlNote;
}
