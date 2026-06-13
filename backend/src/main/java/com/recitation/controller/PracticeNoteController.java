package com.recitation.controller;

import com.recitation.common.Result;
import com.recitation.dto.PracticeNoteDTO;
import com.recitation.entity.PracticeNote;
import com.recitation.service.PracticeNoteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class PracticeNoteController {

    @Resource
    private PracticeNoteService practiceNoteService;

    @PostMapping
    public Result<PracticeNote> save(@Valid @RequestBody PracticeNoteDTO dto) {
        return Result.success(practiceNoteService.saveOrUpdate(dto));
    }

    @GetMapping
    public Result<PracticeNote> getNote(
            @RequestParam Long userId,
            @RequestParam Long manuscriptId) {
        return Result.success(practiceNoteService.getNote(userId, manuscriptId));
    }

    @GetMapping("/user")
    public Result<Page<PracticeNote>> getUserNotes(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(practiceNoteService.getUserNotes(userId, page, size));
    }

    @GetMapping("/manuscript/{manuscriptId}")
    public Result<List<PracticeNote>> getManuscriptNotes(@PathVariable Long manuscriptId) {
        return Result.success(practiceNoteService.getManuscriptNotes(manuscriptId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean deleted = practiceNoteService.deleteNote(id);
        if (!deleted) {
            return Result.error("笔记不存在");
        }
        return Result.success();
    }
}
