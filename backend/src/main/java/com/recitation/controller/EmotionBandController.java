package com.recitation.controller;

import com.recitation.common.Result;
import com.recitation.dto.EmotionBandDTO;
import com.recitation.entity.EmotionBand;
import com.recitation.service.EmotionBandService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emotion-bands")
public class EmotionBandController {

    @Resource
    private EmotionBandService emotionBandService;

    @PostMapping
    public Result<EmotionBand> save(@Valid @RequestBody EmotionBandDTO dto) {
        return Result.success(emotionBandService.saveOrUpdate(dto));
    }

    @GetMapping
    public Result<Map<Integer, Map<String, String>>> getEmotions(
            @RequestParam Long userId,
            @RequestParam Long manuscriptId) {
        return Result.success(emotionBandService.getEmotionMap(userId, manuscriptId));
    }

    @GetMapping("/list")
    public Result<List<EmotionBand>> getEmotionList(
            @RequestParam Long userId,
            @RequestParam Long manuscriptId) {
        return Result.success(emotionBandService.getEmotionList(userId, manuscriptId));
    }

    @GetMapping("/curve")
    public Result<List<Map<String, Object>>> getEmotionCurve(
            @RequestParam Long userId,
            @RequestParam Long manuscriptId) {
        return Result.success(emotionBandService.getEmotionCurve(userId, manuscriptId));
    }

    @DeleteMapping
    public Result<Void> deleteEmotion(
            @RequestParam Long userId,
            @RequestParam Long manuscriptId,
            @RequestParam Integer paragraphIndex) {
        boolean deleted = emotionBandService.deleteEmotion(userId, manuscriptId, paragraphIndex);
        if (!deleted) {
            return Result.error("记录不存在");
        }
        return Result.success();
    }
}
