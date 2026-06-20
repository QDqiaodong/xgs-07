package com.recitation.controller;

import com.recitation.common.Result;
import com.recitation.dto.DifficultyAssessmentDTO;
import com.recitation.enums.DifficultyLevel;
import com.recitation.service.difficulty.DifficultyAssessor;
import com.recitation.service.difficulty.DifficultyRuleLibrary;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/difficulty")
public class DifficultyController {

    @Resource
    private DifficultyAssessor difficultyAssessor;

    @Resource
    private DifficultyRuleLibrary ruleLibrary;

    @PostMapping("/assess")
    public Result<DifficultyAssessmentDTO> assess(@RequestBody Map<String, String> body) {
        String content = body == null ? null : body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("文稿内容不能为空");
        }
        return Result.success(difficultyAssessor.assess(content));
    }

    @GetMapping("/levels")
    public Result<List<Map<String, Object>>> levels() {
        List<Map<String, Object>> levels = new ArrayList<>();
        for (DifficultyLevel level : DifficultyLevel.values()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("level", level.getLevel());
            item.put("label", level.getLabel());
            item.put("description", level.getDescription());
            item.put("length", ruleLibrary.describeLength(level));
            item.put("complexity", ruleLibrary.describeComplexity(level));
            item.put("emotion", ruleLibrary.describeEmotion(level));
            levels.add(item);
        }
        return Result.success(levels);
    }

    @GetMapping("/dimensions")
    public Result<List<Map<String, Object>>> dimensions() {
        List<String> keys = Arrays.asList(
                DifficultyRuleLibrary.DIMENSION_LENGTH,
                DifficultyRuleLibrary.DIMENSION_COMPLEXITY,
                DifficultyRuleLibrary.DIMENSION_EMOTION);
        List<Map<String, Object>> dimensions = new ArrayList<>();
        for (String key : keys) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("dimension", key);
            item.put("label", ruleLibrary.getDimensionLabel(key));
            item.put("weight", ruleLibrary.getWeight(key));
            dimensions.add(item);
        }
        return Result.success(dimensions);
    }
}
