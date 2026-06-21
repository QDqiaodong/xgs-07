package com.recitation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrainingPackageItemDTO {

    private Long id;

    @NotNull(message = "训练包ID不能为空")
    private Long packageId;

    @NotNull(message = "文稿ID不能为空")
    private Long manuscriptId;

    private Integer sortOrder;

    private String stage;

    private String stageNote;

    private String manuscriptTitle;

    private String manuscriptAuthor;

    private String manuscriptDifficulty;
}
