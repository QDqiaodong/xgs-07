package com.recitation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TrainingPackageDTO {

    private Long id;

    @NotBlank(message = "训练包名称不能为空")
    @Size(max = 100, message = "训练包名称不能超过100个字符")
    private String name;

    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;

    private String coverImage;

    private Long categoryId;

    private String categoryName;

    private String difficulty;

    private Integer targetDuration;

    private Integer targetDays;

    private String trainingGoals;

    private String tips;

    private Integer sortOrder;

    private Boolean status;

    private Boolean isPublic;

    private String createUser;

    private List<TrainingPackageItemDTO> items;
}
