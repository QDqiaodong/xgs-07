package com.recitation.service;

import com.recitation.dto.ParagraphProgressDTO;
import com.recitation.entity.ParagraphProgress;
import com.recitation.enums.ParagraphStatus;
import com.recitation.repository.ParagraphProgressRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ParagraphProgressService {

    @Resource
    private ParagraphProgressRepository paragraphProgressRepository;

    @Transactional
    public ParagraphProgress saveOrUpdate(ParagraphProgressDTO dto) {
        if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            paragraphProgressRepository.deleteByUserIdAndManuscriptIdAndParagraphIndex(
                    dto.getUserId(), dto.getManuscriptId(), dto.getParagraphIndex());
            return null;
        }
        if (!ParagraphStatus.isValid(dto.getStatus())) {
            throw new IllegalArgumentException("非法的段落状态值: " + dto.getStatus() + "，允许的值为: " + ParagraphStatus.VALID_VALUES);
        }
        try {
            paragraphProgressRepository.deleteByUserIdAndManuscriptIdAndParagraphIndex(
                    dto.getUserId(), dto.getManuscriptId(), dto.getParagraphIndex());
            ParagraphProgress progress = new ParagraphProgress();
            progress.setUserId(dto.getUserId());
            progress.setManuscriptId(dto.getManuscriptId());
            progress.setParagraphIndex(dto.getParagraphIndex());
            progress.setStatus(dto.getStatus());
            return paragraphProgressRepository.save(progress);
        } catch (DataIntegrityViolationException e) {
            Optional<ParagraphProgress> existing = paragraphProgressRepository
                    .findByUserIdAndManuscriptIdAndParagraphIndex(dto.getUserId(), dto.getManuscriptId(), dto.getParagraphIndex());
            if (existing.isPresent()) {
                ParagraphProgress progress = existing.get();
                progress.setStatus(dto.getStatus());
                return paragraphProgressRepository.save(progress);
            }
            throw e;
        }
    }

    public Map<Integer, String> getProgressMap(Long userId, Long manuscriptId) {
        List<ParagraphProgress> list = paragraphProgressRepository.findByUserIdAndManuscriptId(userId, manuscriptId);
        Map<Integer, String> map = new HashMap<>();
        for (ParagraphProgress p : list) {
            if (ParagraphStatus.isValid(p.getStatus())) {
                map.put(p.getParagraphIndex(), p.getStatus());
            }
        }
        return map;
    }

    public List<ParagraphProgress> getProgressList(Long userId, Long manuscriptId) {
        List<ParagraphProgress> list = paragraphProgressRepository.findByUserIdAndManuscriptId(userId, manuscriptId);
        Map<Integer, ParagraphProgress> uniqueMap = new LinkedHashMap<>();
        for (ParagraphProgress p : list) {
            if (ParagraphStatus.isValid(p.getStatus()) && !uniqueMap.containsKey(p.getParagraphIndex())) {
                uniqueMap.put(p.getParagraphIndex(), p);
            }
        }
        return new ArrayList<>(uniqueMap.values());
    }

    @Transactional
    public boolean deleteProgress(Long userId, Long manuscriptId, Integer paragraphIndex) {
        paragraphProgressRepository.deleteByUserIdAndManuscriptIdAndParagraphIndex(userId, manuscriptId, paragraphIndex);
        return true;
    }
}
