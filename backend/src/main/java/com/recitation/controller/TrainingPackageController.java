package com.recitation.controller;

import com.recitation.common.Result;
import com.recitation.dto.TrainingPackageDTO;
import com.recitation.dto.TrainingPackageProgressDTO;
import com.recitation.entity.TrainingPackage;
import com.recitation.service.TrainingPackageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/training-packages")
public class TrainingPackageController {

    @Resource
    private TrainingPackageService trainingPackageService;

    @GetMapping
    public Result<List<TrainingPackageDTO>> listPackages(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Long userId) {
        return Result.success(trainingPackageService.listPublicPackages(categoryId, difficulty, userId));
    }

    @GetMapping("/{id}")
    public Result<TrainingPackageDTO> getPackageDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        return Result.success(trainingPackageService.getPackageDetail(id, userId));
    }

    @GetMapping("/{id}/with-progress")
    public Result<Map<String, Object>> getPackageWithProgress(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return Result.success(trainingPackageService.getPackageWithProgress(id, userId));
    }

    @PostMapping
    public Result<TrainingPackage> createPackage(@Valid @RequestBody TrainingPackageDTO dto) {
        return Result.success(trainingPackageService.createPackage(dto));
    }

    @PutMapping("/{id}")
    public Result<TrainingPackage> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody TrainingPackageDTO dto) {
        return Result.success(trainingPackageService.updatePackage(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePackage(
            @PathVariable Long id,
            @RequestParam Long userId) {
        boolean deleted = trainingPackageService.deletePackage(id, userId);
        if (!deleted) {
            return Result.error("删除失败");
        }
        return Result.success();
    }

    @PostMapping("/{id}/progress/start")
    public Result<TrainingPackageProgressDTO> startProgress(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return Result.success(trainingPackageService.startOrGetProgress(userId, id));
    }

    @GetMapping("/progress/user")
    public Result<List<TrainingPackageProgressDTO>> getUserProgressList(@RequestParam Long userId) {
        return Result.success(trainingPackageService.getUserProgressList(userId));
    }

    @GetMapping("/{id}/progress")
    public Result<TrainingPackageProgressDTO> getProgress(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return Result.success(trainingPackageService.startOrGetProgress(userId, id));
    }

    @PutMapping("/progress")
    public Result<TrainingPackageProgressDTO> updateProgress(@Valid @RequestBody TrainingPackageProgressDTO dto) {
        return Result.success(trainingPackageService.updateProgress(dto));
    }

    @PostMapping("/{id}/progress/practice")
    public Result<TrainingPackageProgressDTO> recordPractice(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "5") int minutes) {
        return Result.success(trainingPackageService.recordPractice(userId, id, minutes));
    }
}
