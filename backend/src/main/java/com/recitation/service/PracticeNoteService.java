package com.recitation.service;

import com.recitation.dto.PracticeNoteDTO;
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

    @Transactional
    public PracticeNote saveOrUpdate(PracticeNoteDTO dto) {
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
        return practiceNoteRepository.findByUserIdAndManuscriptId(userId, manuscriptId).orElse(null);
    }

    public Page<PracticeNote> getUserNotes(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return practiceNoteRepository.findByUserIdOrderByUpdateTimeDesc(userId, pageable);
    }

    public List<PracticeNote> getManuscriptNotes(Long manuscriptId) {
        return practiceNoteRepository.findByManuscriptIdOrderByCreateTimeDesc(manuscriptId);
    }

    @Transactional
    public boolean deleteNote(Long id) {
        if (practiceNoteRepository.existsById(id)) {
            practiceNoteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
