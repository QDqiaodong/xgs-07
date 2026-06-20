package com.recitation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ManuscriptDTO {

    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过100个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 20000, message = "正文不能超过20000个字符")
    private String content;

    @Size(max = 500, message = "简介不能超过500个字符")
    private String introduction;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private String categoryName;

    private String author;

    private String difficulty;

    private Boolean isPublic;

    private String createUser;
}
