package com.recitation.controller;

import com.recitation.common.Result;
import com.recitation.dto.PronunciationDifficultyDTO;
import com.recitation.entity.PronunciationDifficulty;
import com.recitation.service.PronunciationDifficultyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pronunciation-difficulty")
public class PronunciationDifficultyController {

    @Resource
    private PronunciationDifficultyService pronunciationDifficultyService;

    @PostMapping
    public Result<PronunciationDifficulty> save(@Valid @RequestBody PronunciationDifficultyDTO dto) {
        return Result.success(pronunciationDifficultyService.saveOrUpdate(dto));
    }

    @GetMapping
    public Result<Map<Integer, PronunciationDifficulty>> getDifficultyMap(
            @RequestParam Long manuscriptId,
            @RequestParam(required = false) Long userId) {
        return Result.success(pronunciationDifficultyService.getDifficultyMap(manuscriptId, userId));
    }

    @GetMapping("/list")
    public Result<List<PronunciationDifficulty>> getList(
            @RequestParam Long manuscriptId,
            @RequestParam(required = false) Long userId) {
        return Result.success(pronunciationDifficultyService.getAccessibleByManuscriptAndUser(manuscriptId, userId));
    }

    @GetMapping("/paragraph")
    public Result<PronunciationDifficulty> getByParagraph(
            @RequestParam Long manuscriptId,
            @RequestParam Integer paragraphIndex,
            @RequestParam(required = false) Long userId) {
        return Result.success(pronunciationDifficultyService.getByManuscriptAndParagraphAndUser(
                manuscriptId, paragraphIndex, userId));
    }

    @DeleteMapping
    public Result<Void> deleteDifficulty(
            @RequestParam Long manuscriptId,
            @RequestParam Integer paragraphIndex,
            @RequestParam Long userId) {
        boolean deleted = pronunciationDifficultyService.delete(manuscriptId, paragraphIndex, userId);
        if (!deleted) {
            return Result.error("记录不存在");
        }
        return Result.success();
    }
}
