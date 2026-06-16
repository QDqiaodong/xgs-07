package com.recitation.dto;

import com.recitation.enums.ParagraphStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ParagraphProgressDTO {

    private Long id;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "文稿ID不能为空")
    private Long manuscriptId;

    @NotNull(message = "段落索引不能为空")
    private Integer paragraphIndex;

    @Pattern(regexp = "^(mastered|strengthen|skip)$", message = "状态值不合法，必须是 mastered、strengthen 或 skip")
    private String status;
}
