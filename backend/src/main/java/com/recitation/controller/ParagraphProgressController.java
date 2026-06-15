package com.recitation.controller;

import com.recitation.common.Result;
import com.recitation.dto.ParagraphProgressDTO;
import com.recitation.entity.ParagraphProgress;
import com.recitation.service.ParagraphProgressService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paragraph-progress")
public class ParagraphProgressController {

    @Resource
    private ParagraphProgressService paragraphProgressService;

    @PostMapping
    public Result<ParagraphProgress> save(@Valid @RequestBody ParagraphProgressDTO dto) {
        return Result.success(paragraphProgressService.saveOrUpdate(dto));
    }

    @GetMapping
    public Result<Map<Integer, String>> getProgress(
            @RequestParam Long userId,
            @RequestParam Long manuscriptId) {
        return Result.success(paragraphProgressService.getProgressMap(userId, manuscriptId));
    }

    @GetMapping("/list")
    public Result<List<ParagraphProgress>> getProgressList(
            @RequestParam Long userId,
            @RequestParam Long manuscriptId) {
        return Result.success(paragraphProgressService.getProgressList(userId, manuscriptId));
    }

    @DeleteMapping
    public Result<Void> deleteProgress(
            @RequestParam Long userId,
            @RequestParam Long manuscriptId,
            @RequestParam Integer paragraphIndex) {
        boolean deleted = paragraphProgressService.deleteProgress(userId, manuscriptId, paragraphIndex);
        if (!deleted) {
            return Result.error("记录不存在");
        }
        return Result.success();
    }
}
