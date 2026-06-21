package com.recitation.service;

import com.recitation.common.BusinessException;
import com.recitation.dto.ParagraphProgressDTO;
import com.recitation.dto.ParagraphTrainingStateDTO;
import com.recitation.dto.PracticeCalendarDTO;
import com.recitation.dto.TrainingProgressDTO;
import com.recitation.entity.Manuscript;
import com.recitation.entity.ManuscriptParagraph;
import com.recitation.entity.ParagraphProgress;
import com.recitation.entity.PracticeNote;
import com.recitation.enums.ParagraphStatus;
import com.recitation.repository.ParagraphProgressRepository;
import com.recitation.repository.PracticeNoteRepository;
import com.recitation.utils.ManuscriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class ParagraphProgressService {

    @Resource
    private ParagraphProgressRepository paragraphProgressRepository;

    @Resource
    private PracticeNoteRepository practiceNoteRepository;

    @Resource
    private ManuscriptService manuscriptService;

    @Resource
    private ManuscriptParagraphService manuscriptParagraphService;

    private void validateManuscriptAccess(Long manuscriptId, Long userId) {
        Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId);
        if (manuscript == null) {
            throw new BusinessException("文稿不存在，无法保存段落进度");
        }
        if (manuscript.getStatus() != 1) {
            throw new BusinessException("文稿状态异常，无法保存段落进度");
        }
        String userIdStr = ManuscriptUtils.formatUserId(userId);
        if (!ManuscriptUtils.canAccessManuscript(manuscript, userIdStr)) {
            throw new BusinessException("无权限访问该文稿，无法保存段落进度");
        }
    }

    @Transactional
    public ParagraphProgress saveOrUpdate(ParagraphProgressDTO dto) {
        validateManuscriptAccess(dto.getManuscriptId(), dto.getUserId());
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
        validateManuscriptAccess(manuscriptId, userId);
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
        validateManuscriptAccess(manuscriptId, userId);
        List<ParagraphProgress> list = paragraphProgressRepository.findByUserIdAndManuscriptId(userId, manuscriptId);
        Map<Integer, ParagraphProgress> uniqueMap = new LinkedHashMap<>();
        for (ParagraphProgress p : list) {
            if (ParagraphStatus.isValid(p.getStatus()) && !uniqueMap.containsKey(p.getParagraphIndex())) {
                uniqueMap.put(p.getParagraphIndex(), p);
            }
        }
        return new ArrayList<>(uniqueMap.values());
    }

    public ParagraphTrainingStateDTO getTrainingState(Long userId, Long manuscriptId) {
        validateManuscriptAccess(manuscriptId, userId);

        Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId, ManuscriptUtils.formatUserId(userId));

        ParagraphTrainingStateDTO dto = new ParagraphTrainingStateDTO();
        dto.setManuscriptId(manuscriptId);
        if (manuscript != null) {
            dto.setManuscriptTitle(manuscript.getTitle());
        }

        TreeMap<Integer, ManuscriptParagraph> paragraphMap = new TreeMap<>();
        List<ManuscriptParagraph> stored = manuscriptParagraphService.getParagraphList(manuscriptId);
        if (stored != null) {
            for (ManuscriptParagraph mp : stored) {
                if (mp.getParagraphIndex() != null) {
                    paragraphMap.put(mp.getParagraphIndex(), mp);
                }
            }
        }

        List<String> contents = new ArrayList<>();
        if (!paragraphMap.isEmpty()) {
            for (ManuscriptParagraph mp : paragraphMap.values()) {
                contents.add(mp.getContent() != null ? mp.getContent() : "");
            }
        } else if (manuscript != null) {
            String categoryName = manuscript.getCategoryName() != null ? manuscript.getCategoryName() : "";
            String content = manuscript.getContent() != null ? manuscript.getContent() : "";
            contents = ManuscriptUtils.splitParagraphContents(content, categoryName);
        }

        int total = contents.size();
        dto.setTotalParagraphs(total);

        Map<Integer, String> statusMap = getProgressMap(userId, manuscriptId);

        List<ParagraphTrainingStateDTO.ParagraphInfo> paragraphs = new ArrayList<>();
        List<Integer> completed = new ArrayList<>();
        List<Integer> strengthen = new ArrayList<>();
        List<Integer> skip = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            String status = statusMap.get(i);
            ParagraphTrainingStateDTO.ParagraphInfo info = new ParagraphTrainingStateDTO.ParagraphInfo();
            info.setParagraphIndex(i);
            info.setContent(contents.get(i));

            ManuscriptParagraph mp = paragraphMap.get(i);
            if (mp != null) {
                info.setReadingTip(mp.getReadingTip() != null ? mp.getReadingTip() : "");
                info.setPracticeFocus(mp.getPracticeFocus() != null ? mp.getPracticeFocus() : "");
            } else {
                info.setReadingTip("");
                info.setPracticeFocus("");
            }
            info.setStatus(status != null ? status : "");
            paragraphs.add(info);

            if (ParagraphStatus.MASTERED.getValue().equals(status)) {
                completed.add(i);
            } else if (ParagraphStatus.STRENGTHEN.getValue().equals(status)) {
                strengthen.add(i);
            } else if (ParagraphStatus.SKIP.getValue().equals(status)) {
                skip.add(i);
            }
        }

        dto.setParagraphs(paragraphs);
        dto.setCompletedParagraphs(completed);
        dto.setStrengthenParagraphs(strengthen);
        dto.setSkipParagraphs(skip);

        int current = -1;
        for (int i = 0; i < total; i++) {
            String status = statusMap.get(i);
            if (!ParagraphStatus.MASTERED.getValue().equals(status)
                    && !ParagraphStatus.SKIP.getValue().equals(status)) {
                current = i;
                break;
            }
        }
        if (current == -1) {
            current = total > 0 ? total - 1 : 0;
            dto.setTrainingCompleted(true);
        }
        dto.setCurrentParagraphIndex(current);

        if (total > 0) {
            dto.setProgressPercent(Math.round((completed.size() * 100.0f) / total));
        }

        return dto;
    }

    @Transactional
    public boolean deleteProgress(Long userId, Long manuscriptId, Integer paragraphIndex) {
        paragraphProgressRepository.deleteByUserIdAndManuscriptIdAndParagraphIndex(userId, manuscriptId, paragraphIndex);
        return true;
    }

    public TrainingProgressDTO getTrainingProgress(Long userId, Long manuscriptId) {
        validateManuscriptAccess(manuscriptId, userId);
        TrainingProgressDTO dto = new TrainingProgressDTO();
        dto.setManuscriptId(manuscriptId);

        Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId, ManuscriptUtils.formatUserId(userId));
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

            Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId, ManuscriptUtils.formatUserId(userId));
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

    public List<PracticeCalendarDTO> getPracticeCalendar(Long userId, Integer year, Integer month) {
        List<ParagraphProgress> allProgress = paragraphProgressRepository.findByUserId(userId);
        List<PracticeNote> allNotes = practiceNoteRepository.findByUserIdOrderByUpdateTimeDesc(userId);

        Map<LocalDate, List<ParagraphProgress>> progressByDate = new LinkedHashMap<>();
        Map<LocalDate, List<PracticeNote>> notesByDate = new LinkedHashMap<>();

        for (ParagraphProgress p : allProgress) {
            if (p.getUpdateTime() != null) {
                LocalDate date = p.getUpdateTime().toLocalDate();
                progressByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(p);
            }
            if (p.getCreateTime() != null && !p.getCreateTime().toLocalDate().equals(p.getUpdateTime() != null ? p.getUpdateTime().toLocalDate() : null)) {
                LocalDate date = p.getCreateTime().toLocalDate();
                progressByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(p);
            }
        }

        for (PracticeNote note : allNotes) {
            if (note.getUpdateTime() != null) {
                LocalDate date = note.getUpdateTime().toLocalDate();
                notesByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(note);
            }
            if (note.getCreateTime() != null && !note.getCreateTime().toLocalDate().equals(note.getUpdateTime() != null ? note.getUpdateTime().toLocalDate() : null)) {
                LocalDate date = note.getCreateTime().toLocalDate();
                notesByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(note);
            }
        }

        LocalDate startDate;
        LocalDate endDate;
        if (year != null && month != null) {
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.plusMonths(1).minusDays(1);
        } else {
            LocalDate now = LocalDate.now();
            startDate = now.minusMonths(2).withDayOfMonth(1);
            endDate = now.plusMonths(1).withDayOfMonth(now.plusMonths(1).lengthOfMonth());
        }

        Map<LocalDate, Map<Long, PracticeCalendarDTO.ManuscriptPracticeInfo>> dailyManuscriptMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dailyManuscriptMap.put(date, new LinkedHashMap<>());
        }

        for (Map.Entry<LocalDate, List<ParagraphProgress>> entry : progressByDate.entrySet()) {
            LocalDate date = entry.getKey();
            if (date.isBefore(startDate) || date.isAfter(endDate)) continue;

            Map<Long, List<ParagraphProgress>> progressByManuscript = entry.getValue().stream()
                    .collect(Collectors.groupingBy(ParagraphProgress::getManuscriptId));

            for (Map.Entry<Long, List<ParagraphProgress>> manuscriptEntry : progressByManuscript.entrySet()) {
                Long manuscriptId = manuscriptEntry.getKey();
                int totalPractice = manuscriptEntry.getValue().stream()
                        .mapToInt(p -> p.getPracticeCount() != null ? p.getPracticeCount() : 0)
                        .sum();

                Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId, ManuscriptUtils.formatUserId(userId));
                if (manuscript == null || manuscript.getStatus() != 1) continue;

                PracticeCalendarDTO.ManuscriptPracticeInfo info = new PracticeCalendarDTO.ManuscriptPracticeInfo();
                info.setManuscriptId(manuscriptId);
                info.setManuscriptTitle(manuscript.getTitle());
                info.setPracticeCount(totalPractice);

                dailyManuscriptMap.get(date).put(manuscriptId, info);
            }
        }

        for (Map.Entry<LocalDate, List<PracticeNote>> entry : notesByDate.entrySet()) {
            LocalDate date = entry.getKey();
            if (date.isBefore(startDate) || date.isAfter(endDate)) continue;

            Map<Long, PracticeNote> noteByManuscript = new LinkedHashMap<>();
            for (PracticeNote note : entry.getValue()) {
                if (!noteByManuscript.containsKey(note.getManuscriptId())) {
                    noteByManuscript.put(note.getManuscriptId(), note);
                }
            }

            for (Map.Entry<Long, PracticeNote> noteEntry : noteByManuscript.entrySet()) {
                Long manuscriptId = noteEntry.getKey();
                PracticeNote note = noteEntry.getValue();

                Manuscript manuscript = manuscriptService.getManuscriptById(manuscriptId, ManuscriptUtils.formatUserId(userId));
                if (manuscript == null || manuscript.getStatus() != 1) continue;

                Map<Long, PracticeCalendarDTO.ManuscriptPracticeInfo> dayMap = dailyManuscriptMap.computeIfAbsent(date, k -> new LinkedHashMap<>());
                PracticeCalendarDTO.ManuscriptPracticeInfo info = dayMap.computeIfAbsent(manuscriptId, k -> {
                    PracticeCalendarDTO.ManuscriptPracticeInfo newInfo = new PracticeCalendarDTO.ManuscriptPracticeInfo();
                    newInfo.setManuscriptId(manuscriptId);
                    newInfo.setManuscriptTitle(manuscript.getTitle());
                    newInfo.setPracticeCount(0);
                    return newInfo;
                });

                info.setEmotionScore(note.getEmotionControlScore());

                StringBuilder noteSummary = new StringBuilder();
                if (note.getDifficultyPoints() != null && !note.getDifficultyPoints().isBlank()) {
                    noteSummary.append("难点：").append(truncateText(note.getDifficultyPoints(), 30)).append("; ");
                }
                if (note.getToneControl() != null && !note.getToneControl().isBlank()) {
                    noteSummary.append("语气：").append(truncateText(note.getToneControl(), 30)).append("; ");
                }
                if (note.getEmotionExpression() != null && !note.getEmotionExpression().isBlank()) {
                    noteSummary.append("情感：").append(truncateText(note.getEmotionExpression(), 30)).append("; ");
                }
                if (note.getOtherNotes() != null && !note.getOtherNotes().isBlank()) {
                    noteSummary.append("心得：").append(truncateText(note.getOtherNotes(), 30)).append("; ");
                }
                if (note.getEmotionControlNote() != null && !note.getEmotionControlNote().isBlank()) {
                    noteSummary.append("情绪：").append(truncateText(note.getEmotionControlNote(), 30));
                }
                info.setNoteSummary(noteSummary.toString().trim());
            }
        }

        List<PracticeCalendarDTO> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<Long, PracticeCalendarDTO.ManuscriptPracticeInfo> dayMap = dailyManuscriptMap.get(date);
            if (dayMap == null || dayMap.isEmpty()) continue;

            PracticeCalendarDTO dto = new PracticeCalendarDTO();
            dto.setDate(date.format(formatter));
            dto.setManuscriptCount(dayMap.size());

            List<PracticeCalendarDTO.ManuscriptPracticeInfo> manuscriptList = new ArrayList<>(dayMap.values());
            dto.setManuscripts(manuscriptList);

            int totalPractice = manuscriptList.stream()
                    .mapToInt(m -> m.getPracticeCount() != null ? m.getPracticeCount() : 0)
                    .sum();
            dto.setTotalPracticeCount(totalPractice);
            dto.setEstimatedMinutes(totalPractice * 3);

            List<String> keyPointsList = new ArrayList<>();
            for (PracticeCalendarDTO.ManuscriptPracticeInfo info : manuscriptList) {
                if (info.getNoteSummary() != null && !info.getNoteSummary().isBlank()) {
                    keyPointsList.add(info.getManuscriptTitle() + " - " + truncateText(info.getNoteSummary(), 50));
                } else if (info.getEmotionScore() != null) {
                    keyPointsList.add(info.getManuscriptTitle() + " - 情绪评分" + info.getEmotionScore() + "分");
                } else if (info.getPracticeCount() != null && info.getPracticeCount() > 0) {
                    keyPointsList.add(info.getManuscriptTitle() + " - 练习" + info.getPracticeCount() + "次");
                }
            }
            dto.setKeyPoints(keyPointsList.isEmpty() ? manuscriptList.get(0).getManuscriptTitle() + "等" + dayMap.size() + "篇文稿" 
                    : String.join(" | ", keyPointsList.subList(0, Math.min(3, keyPointsList.size()))));

            result.add(dto);
        }

        result.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        return result;
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        text = text.replaceAll("\\s+", " ").trim();
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
