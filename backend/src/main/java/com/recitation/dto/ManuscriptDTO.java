package com.recitation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManuscriptDTO {

    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String introduction;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private String categoryName;

    private String author;

    private String difficulty;

    private Boolean isPublic;

    private String createUser;
}
