package com.recitation.service;

import com.recitation.common.BusinessException;
import com.recitation.dto.PracticeNoteDTO;
import com.recitation.entity.Manuscript;
import com.recitation.entity.PracticeNote;
import com.recitation.repository.PracticeNoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class PracticeNoteService {

    @Resource
    private PracticeNoteRepository practiceNoteRepository;

    @Resource
    private ManuscriptService manuscriptService;

    private void validateManuscriptAccess(Long manuscriptId, Long userId) {
        Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId);
        if (manuscript == null) {
            throw new BusinessException("文稿不存在，无法保存笔记");
        }
        if (manuscript.getStatus() != 1) {
            throw new BusinessException("文稿状态异常，无法保存笔记");
        }
        String userIdStr = String.valueOf(userId);
        boolean isOwner = manuscript.getCreateUser() != null && manuscript.getCreateUser().equals(userIdStr);
        if (!manuscript.getIsPublic() && !isOwner) {
            throw new BusinessException("无权限访问该文稿，无法保存笔记");
        }
    }

    @Transactional
    public PracticeNote saveOrUpdate(PracticeNoteDTO dto) {
        validateManuscriptAccess(dto.getManuscriptId(), dto.getUserId());
        Optional<PracticeNote> existing = practiceNoteRepository.findByUserIdAndManuscriptId(dto.getUserId(), dto.getManuscriptId());
        PracticeNote note;
        if (existing.isPresent()) {
            note = existing.get();
        } else {
            note = new PracticeNote();
            note.setUserId(dto.getUserId());
            note.setManuscriptId(dto.getManuscriptId());
        }
        note.setDifficultyPoints(dto.getDifficultyPoints());
        note.setToneControl(dto.getToneControl());
        note.setEmotionExpression(dto.getEmotionExpression());
        note.setOtherNotes(dto.getOtherNotes());
        return practiceNoteRepository.save(note);
    }

    public PracticeNote getNote(Long userId, Long manuscriptId) {
        Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId);
        if (manuscript == null || manuscript.getStatus() != 1) {
            return null;
        }
        return practiceNoteRepository.findByUserIdAndManuscriptId(userId, manuscriptId).orElse(null);
    }

    public Page<PracticeNote> getUserNotes(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return practiceNoteRepository.findByUserIdOrderByUpdateTimeDesc(userId, pageable);
    }

    public List<PracticeNote> getManuscriptNotes(Long manuscriptId) {
        Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId);
        if (manuscript == null || manuscript.getStatus() != 1) {
            throw new BusinessException("文稿不存在或已下架");
        }
        if (!manuscript.getIsPublic()) {
            throw new BusinessException("该文稿为私有，无法查看笔记");
        }
        return practiceNoteRepository.findByManuscriptIdOrderByCreateTimeDesc(manuscriptId);
    }

    @Transactional
    public boolean deleteNote(Long id, Long userId) {
        Optional<PracticeNote> noteOpt = practiceNoteRepository.findById(id);
        if (noteOpt.isEmpty()) {
            return false;
        }
        PracticeNote note = noteOpt.get();
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除该笔记");
        }
        practiceNoteRepository.deleteById(id);
        return true;
    }
}
