package com.recitation.controller;

import com.recitation.common.Result;
import com.recitation.dto.PracticeSessionDTO;
import com.recitation.entity.PracticeSession;
import com.recitation.service.PracticeSessionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/practice-sessions")
public class PracticeSessionController {

    @Resource
    private PracticeSessionService practiceSessionService;

    @PostMapping("/start")
    public Result<PracticeSession> startSession(@Valid @RequestBody PracticeSessionDTO dto) {
        return Result.success(practiceSessionService.startSession(dto));
    }

    @PostMapping("/{id}/end")
    public Result<PracticeSession> endSession(
            @PathVariable Long id,
            @RequestBody PracticeSessionDTO dto) {
        PracticeSession session = practiceSessionService.endSession(id, dto);
        if (session == null) {
            return Result.error("会话不存在");
        }
        return Result.success(session);
    }

    @PostMapping
    public Result<PracticeSession> save(@Valid @RequestBody PracticeSessionDTO dto) {
        return Result.success(practiceSessionService.saveOrUpdate(dto));
    }

    @GetMapping
    public Result<List<PracticeSession>> getList(
            @RequestParam Long userId,
            @RequestParam(required = false) Long manuscriptId) {
        if (manuscriptId != null) {
            return Result.success(practiceSessionService.getByUserAndManuscript(userId, manuscriptId));
        }
        return Result.success(practiceSessionService.getByUser(userId));
    }

    @GetMapping("/date")
    public Result<List<PracticeSession>> getByDate(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(practiceSessionService.getByUserAndDate(userId, date));
    }

    @GetMapping("/range")
    public Result<List<PracticeSession>> getByDateRange(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(practiceSessionService.getByUserAndDateRange(userId, startDate, endDate));
    }

    @GetMapping("/latest")
    public Result<PracticeSession> getLatest(
            @RequestParam Long userId,
            @RequestParam Long manuscriptId) {
        return Result.success(practiceSessionService.getLatestSession(userId, manuscriptId));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @RequestParam Long userId,
            @RequestParam Long manuscriptId) {
        return Result.success(practiceSessionService.getSessionStats(userId, manuscriptId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSession(
            @PathVariable Long id,
            @RequestParam Long userId) {
        boolean deleted = practiceSessionService.deleteSession(id, userId);
        if (!deleted) {
            return Result.error("记录不存在或无权限删除");
        }
        return Result.success();
    }
}
