package com.recitation.controller;

import com.recitation.common.Result;
import com.recitation.dto.ManuscriptDTO;
import com.recitation.entity.Manuscript;
import com.recitation.service.ManuscriptService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/manuscripts")
public class ManuscriptController {

    @Resource
    private ManuscriptService manuscriptService;

    @PostMapping
    public Result<Manuscript> create(@Valid @RequestBody ManuscriptDTO dto) {
        Manuscript manuscript = manuscriptService.createManuscript(dto);
        return Result.success(manuscript);
    }

    @PutMapping("/{id}")
    public Result<Manuscript> update(@PathVariable Long id, @Valid @RequestBody ManuscriptDTO dto) {
        Manuscript manuscript = manuscriptService.updateManuscript(id, dto);
        if (manuscript == null) {
            return Result.error("文稿不存在");
        }
        return Result.success(manuscript);
    }

    @GetMapping("/{id}")
    public Result<Manuscript> getById(@PathVariable Long id, @RequestParam(required = false) String userId) {
        Manuscript manuscript = manuscriptService.getManuscriptById(id, userId);
        if (manuscript == null) {
            return Result.error("文稿不存在或无权限访问");
        }
        return Result.success(manuscript);
    }

    @GetMapping("/{id}/detail")
    public Result<Manuscript> getDetail(@PathVariable Long id, @RequestParam(required = false) String userId) {
        Manuscript manuscript = manuscriptService.getManuscriptDetail(id, userId);
        if (manuscript == null) {
            return Result.error("文稿不存在或无权限访问");
        }
        return Result.success(manuscript);
    }

    @GetMapping
    public Result<Page<Manuscript>> getPublicList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(manuscriptService.getPublicManuscripts(categoryId, page, size));
    }

    @GetMapping("/my")
    public Result<Page<Manuscript>> getMyList(
            @RequestParam String userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(manuscriptService.getUserManuscripts(userId, categoryId, page, size));
    }

    @GetMapping("/hot")
    public Result<List<Manuscript>> getHotList() {
        return Result.success(manuscriptService.getHotManuscripts());
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestParam(required = false) String userId) {
        boolean deleted = manuscriptService.deleteManuscript(id, userId);
        if (!deleted) {
            return Result.error("文稿不存在或无权限操作");
        }
        return Result.success();
    }
}
