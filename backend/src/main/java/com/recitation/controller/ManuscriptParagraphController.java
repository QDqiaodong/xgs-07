package com.recitation.controller;

import com.recitation.common.Result;
import com.recitation.dto.ManuscriptParagraphDTO;
import com.recitation.entity.ManuscriptParagraph;
import com.recitation.service.ManuscriptParagraphService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/manuscript-paragraphs")
public class ManuscriptParagraphController {

    @Resource
    private ManuscriptParagraphService manuscriptParagraphService;

    @PostMapping
    public Result<ManuscriptParagraph> save(@Valid @RequestBody ManuscriptParagraphDTO dto) {
        return Result.success(manuscriptParagraphService.saveOrUpdate(dto));
    }

    @GetMapping
    public Result<List<ManuscriptParagraph>> getList(@RequestParam Long manuscriptId) {
        return Result.success(manuscriptParagraphService.getParagraphList(manuscriptId));
    }

    @DeleteMapping
    public Result<Void> delete(@RequestParam Long manuscriptId, @RequestParam Integer paragraphIndex) {
        boolean deleted = manuscriptParagraphService.deleteParagraph(manuscriptId, paragraphIndex);
        if (!deleted) {
            return Result.error("段落记录不存在");
        }
        return Result.success();
    }
}
