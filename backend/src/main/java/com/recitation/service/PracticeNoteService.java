package com.recitation.service;

import com.recitation.common.BusinessException;
import com.recitation.dto.PracticeNoteDTO;
import com.recitation.entity.Manuscript;
import com.recitation.entity.PracticeNote;
import com.recitation.repository.PracticeNoteRepository;
import com.recitation.utils.ManuscriptUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PracticeNoteService {

    @Resource
    private PracticeNoteRepository practiceNoteRepository;

    @Resource
    private ManuscriptService manuscriptService;

    private void validateManuscriptAccess(Long manuscriptId, Long userId) {
        String userIdStr = ManuscriptUtils.formatUserId(userId);
        Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId, userIdStr);
        if (manuscript == null) {
            throw new BusinessException("文稿不存在或无权限访问，无法保存笔记");
        }
        if (manuscript.getStatus() != 1) {
            throw new BusinessException("文稿状态异常，无法保存笔记");
        }
    }

    @Transactional
    public PracticeNote saveOrUpdate(PracticeNoteDTO dto) {
        validateManuscriptAccess(dto.getManuscriptId(), dto.getUserId());
        practiceNoteRepository.upsertByUniqueKey(
                dto.getUserId(),
                dto.getManuscriptId(),
                dto.getDifficultyPoints(),
                dto.getToneControl(),
                dto.getEmotionExpression(),
                dto.getOtherNotes(),
                dto.getEmotionControlScore(),
                dto.getEmotionControlNote());
        return practiceNoteRepository
                .findByUserIdAndManuscriptId(dto.getUserId(), dto.getManuscriptId())
                .orElse(null);
    }

    public PracticeNote getNote(Long userId, Long manuscriptId) {
        String userIdStr = ManuscriptUtils.formatUserId(userId);
        Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId, userIdStr);
        if (manuscript == null || manuscript.getStatus() != 1) {
            return null;
        }
        return practiceNoteRepository.findByUserIdAndManuscriptId(userId, manuscriptId).orElse(null);
    }

    public Page<PracticeNote> getUserNotes(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<PracticeNote> allNotes = practiceNoteRepository.findByUserIdOrderByUpdateTimeDesc(userId);
        String userIdStr = ManuscriptUtils.formatUserId(userId);
        
        Map<Long, PracticeNote> uniqueMap = new LinkedHashMap<>();
        for (PracticeNote note : allNotes) {
            if (!uniqueMap.containsKey(note.getManuscriptId())) {
                Manuscript manuscript = manuscriptService.getManuscriptById(note.getManuscriptId(), userIdStr);
                if (manuscript != null && manuscript.getStatus() == 1) {
                    uniqueMap.put(note.getManuscriptId(), note);
                }
            }
        }
        
        List<PracticeNote> uniqueList = new ArrayList<>(uniqueMap.values());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), uniqueList.size());
        List<PracticeNote> pageContent = start >= uniqueList.size() ? new ArrayList<>() : uniqueList.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, uniqueList.size());
    }

    public List<PracticeNote> getManuscriptNotes(Long manuscriptId) {
        Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId);
        if (manuscript == null || manuscript.getStatus() != 1) {
            throw new BusinessException("文稿不存在或已下架");
        }
        if (!Boolean.TRUE.equals(manuscript.getIsPublic())) {
            throw new BusinessException("该文稿为私有，无法查看笔记");
        }
        return practiceNoteRepository.findByManuscriptIdOrderByCreateTimeDesc(manuscriptId);
    }

    @Transactional
    public boolean deleteNote(Long id, Long userId) {
        PracticeNote note = practiceNoteRepository.findById(id).orElse(null);
        if (note == null) {
            return false;
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除该笔记");
        }
        practiceNoteRepository.deleteById(id);
        return true;
    }

    public List<PracticeNote> getUserEmotionScoreTrend(Long userId) {
        List<PracticeNote> allNotes = practiceNoteRepository.findByUserIdOrderByUpdateTimeDesc(userId);
        List<PracticeNote> result = new ArrayList<>();
        String userIdStr = ManuscriptUtils.formatUserId(userId);
        for (PracticeNote note : allNotes) {
            if (note.getEmotionControlScore() != null) {
                Manuscript manuscript = manuscriptService.getManuscriptById(note.getManuscriptId(), userIdStr);
                if (manuscript != null && manuscript.getStatus() == 1) {
                    result.add(note);
                }
            }
        }
        return result;
    }
}
