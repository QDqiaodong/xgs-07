package com.recitation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PronunciationDifficultyDTO {

    private Long id;

    @NotNull(message = "文稿ID不能为空")
    private Long manuscriptId;

    @NotNull(message = "段落索引不能为空")
    private Integer paragraphIndex;

    private Long userId;

    private Boolean isPublic = false;

    private String polyphonicWords;

    private String linking;

    private String stress;

    private String breathPoints;
}
