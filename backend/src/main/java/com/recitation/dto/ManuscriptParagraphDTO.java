package com.recitation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManuscriptParagraphDTO {

    private Long id;

    @NotNull(message = "文稿ID不能为空")
    private Long manuscriptId;

    @NotNull(message = "段落序号不能为空")
    private Integer paragraphIndex;

    private String content;

    private String readingTip;

    private String practiceFocus;
}
