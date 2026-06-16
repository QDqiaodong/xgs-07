package com.recitation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmotionBandDTO {

    private Long id;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "文稿ID不能为空")
    private Long manuscriptId;

    @NotNull(message = "段落索引不能为空")
    private Integer paragraphIndex;

    private String emotionType;

    private String remark;
}
