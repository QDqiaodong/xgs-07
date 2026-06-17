package com.recitation.service;

import com.recitation.dto.ParagraphProgressDTO;
import com.recitation.dto.TrainingProgressDTO;
import com.recitation.entity.Manuscript;
import com.recitation.entity.ParagraphProgress;
import com.recitation.entity.PracticeNote;
import com.recitation.enums.ParagraphStatus;
import com.recitation.repository.ParagraphProgressRepository;
import com.recitation.repository.PracticeNoteRepository;
import com.recitation.utils.ManuscriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParagraphProgressService {

    @Resource
    private ParagraphProgressRepository paragraphProgressRepository;

    @Resource
    private PracticeNoteRepository practiceNoteRepository;

    @Resource
    private ManuscriptService manuscriptService;

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
        paragraphProgressRepository.upsertByUniqueKey(
                dto.getUserId(), dto.getManuscriptId(), dto.getParagraphIndex(), dto.getStatus());
        return paragraphProgressRepository
                .findByUserIdAndManuscriptIdAndParagraphIndex(
                        dto.getUserId(), dto.getManuscriptId(), dto.getParagraphIndex())
                .orElse(null);
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

    public TrainingProgressDTO getTrainingProgress(Long userId, Long manuscriptId) {
        TrainingProgressDTO dto = new TrainingProgressDTO();
        dto.setManuscriptId(manuscriptId);

        Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId);
        if (manuscript != null) {
            String categoryName = manuscript.getCategoryName() != null ? manuscript.getCategoryName() : "";
            String content = manuscript.getContent() != null ? manuscript.getContent() : "";
            int totalParagraphs = ManuscriptUtils.countParagraphs(content, categoryName);
            dto.setTotalParagraphs(totalParagraphs);
        }

        List<ParagraphProgress> progressList = paragraphProgressRepository.findByUserIdAndManuscriptId(userId, manuscriptId);
        Map<Integer, ParagraphProgress> uniqueMap = new LinkedHashMap<>();
        for (ParagraphProgress p : progressList) {
            if (ParagraphStatus.isValid(p.getStatus()) && !uniqueMap.containsKey(p.getParagraphIndex())) {
                uniqueMap.put(p.getParagraphIndex(), p);
            }
        }

        int mastered = 0, strengthen = 0, skip = 0;
        for (ParagraphProgress p : uniqueMap.values()) {
            if (ParagraphStatus.MASTERED.getValue().equals(p.getStatus())) {
                mastered++;
            } else if (ParagraphStatus.STRENGTHEN.getValue().equals(p.getStatus())) {
                strengthen++;
            } else if (ParagraphStatus.SKIP.getValue().equals(p.getStatus())) {
                skip++;
            }
        }

        dto.setMasteredCount(mastered);
        dto.setStrengthenCount(strengthen);
        dto.setSkipCount(skip);

        if (dto.getTotalParagraphs() > 0) {
            dto.setProgressPercent(Math.round((mastered * 100.0f) / dto.getTotalParagraphs()));
        }

        Integer totalPractice = paragraphProgressRepository.sumPracticeCountByUserAndManuscript(userId, manuscriptId);
        dto.setTotalPracticeCount(totalPractice != null ? totalPractice : 0);

        LocalDateTime lastPracticeTime = paragraphProgressRepository.findLastUpdateTimeByUserAndManuscript(userId, manuscriptId);
        dto.setLastPracticeTime(lastPracticeTime);

        PracticeNote note = practiceNoteRepository.findByUserIdAndManuscriptId(userId, manuscriptId).orElse(null);
        if (note != null) {
            dto.setHasNote(true);
            dto.setNoteUpdateTime(note.getUpdateTime());
            if (dto.getLastPracticeTime() == null || 
                (dto.getNoteUpdateTime() != null && dto.getNoteUpdateTime().isAfter(dto.getLastPracticeTime()))) {
                dto.setLastPracticeTime(dto.getNoteUpdateTime());
            }
        }

        return dto;
    }

    public List<TrainingProgressDTO> getUserTrainingProgressList(Long userId) {
        List<ParagraphProgress> allProgress = paragraphProgressRepository.findByUserId(userId);
        List<PracticeNote> allNotes = practiceNoteRepository.findByUserIdOrderByUpdateTimeDesc(userId);

        Map<Long, List<ParagraphProgress>> progressByManuscript = new LinkedHashMap<>();
        for (ParagraphProgress p : allProgress) {
            progressByManuscript.computeIfAbsent(p.getManuscriptId(), k -> new ArrayList<>()).add(p);
        }

        Map<Long, PracticeNote> noteByManuscript = new HashMap<>();
        for (PracticeNote note : allNotes) {
            noteByManuscript.putIfAbsent(note.getManuscriptId(), note);
            if (!progressByManuscript.containsKey(note.getManuscriptId())) {
                progressByManuscript.put(note.getManuscriptId(), new ArrayList<>());
            }
        }

        List<TrainingProgressDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<ParagraphProgress>> entry : progressByManuscript.entrySet()) {
            Long manuscriptId = entry.getKey();
            List<ParagraphProgress> progressList = entry.getValue();

            TrainingProgressDTO dto = new TrainingProgressDTO();
            dto.setManuscriptId(manuscriptId);

            Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId);
            if (manuscript != null && manuscript.getStatus() == 1) {
                String categoryName = manuscript.getCategoryName() != null ? manuscript.getCategoryName() : "";
                String content = manuscript.getContent() != null ? manuscript.getContent() : "";
                int totalParagraphs = ManuscriptUtils.countParagraphs(content, categoryName);
                dto.setTotalParagraphs(totalParagraphs);
            } else {
                continue;
            }

            Map<Integer, ParagraphProgress> uniqueMap = new LinkedHashMap<>();
            for (ParagraphProgress p : progressList) {
                if (ParagraphStatus.isValid(p.getStatus()) && !uniqueMap.containsKey(p.getParagraphIndex())) {
                    uniqueMap.put(p.getParagraphIndex(), p);
                }
            }

            int mastered = 0, strengthen = 0, skip = 0;
            for (ParagraphProgress p : uniqueMap.values()) {
                if (ParagraphStatus.MASTERED.getValue().equals(p.getStatus())) {
                    mastered++;
                } else if (ParagraphStatus.STRENGTHEN.getValue().equals(p.getStatus())) {
                    strengthen++;
                } else if (ParagraphStatus.SKIP.getValue().equals(p.getStatus())) {
                    skip++;
                }
            }

            dto.setMasteredCount(mastered);
            dto.setStrengthenCount(strengthen);
            dto.setSkipCount(skip);

            if (dto.getTotalParagraphs() > 0) {
                dto.setProgressPercent(Math.round((mastered * 100.0f) / dto.getTotalParagraphs()));
            }

            Integer totalPractice = paragraphProgressRepository.sumPracticeCountByUserAndManuscript(userId, manuscriptId);
            dto.setTotalPracticeCount(totalPractice != null ? totalPractice : 0);

            LocalDateTime lastPracticeTime = paragraphProgressRepository.findLastUpdateTimeByUserAndManuscript(userId, manuscriptId);
            dto.setLastPracticeTime(lastPracticeTime);

            PracticeNote note = noteByManuscript.get(manuscriptId);
            if (note != null) {
                dto.setHasNote(true);
                dto.setNoteUpdateTime(note.getUpdateTime());
                if (dto.getLastPracticeTime() == null || 
                    (dto.getNoteUpdateTime() != null && dto.getNoteUpdateTime().isAfter(dto.getLastPracticeTime()))) {
                    dto.setLastPracticeTime(dto.getNoteUpdateTime());
                }
            }

            result.add(dto);
        }

        result.sort((a, b) -> {
            LocalDateTime timeA = a.getLastPracticeTime();
            LocalDateTime timeB = b.getLastPracticeTime();
            if (timeA == null && timeB == null) return 0;
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });

        return result;
    }
}
