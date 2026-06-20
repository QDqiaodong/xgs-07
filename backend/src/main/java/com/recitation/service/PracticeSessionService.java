package com.recitation.service;

import com.recitation.dto.PracticeSessionDTO;
import com.recitation.entity.PracticeSession;
import com.recitation.repository.PracticeSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PracticeSessionService {

    @Resource
    private PracticeSessionRepository practiceSessionRepository;

    @Transactional
    public PracticeSession startSession(PracticeSessionDTO dto) {
        PracticeSession session = new PracticeSession();
        session.setUserId(dto.getUserId());
        session.setManuscriptId(dto.getManuscriptId());
        session.setStartTime(dto.getStartTime());
        session.setSessionDate(dto.getStartTime().toLocalDate());
        return practiceSessionRepository.save(session);
    }

    @Transactional
    public PracticeSession endSession(Long sessionId, PracticeSessionDTO dto) {
        PracticeSession session = practiceSessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return null;
        }
        session.setEndTime(dto.getEndTime());
        if (dto.getEndTime() != null && session.getStartTime() != null) {
            session.setDurationSeconds((int) Duration.between(session.getStartTime(), dto.getEndTime()).getSeconds());
        } else if (dto.getDurationSeconds() != null) {
            session.setDurationSeconds(dto.getDurationSeconds());
        }
        session.setCompletedParagraphs(dto.getCompletedParagraphs());
        session.setSelfAssessmentStatus(dto.getSelfAssessmentStatus());
        session.setSelfAssessmentScore(dto.getSelfAssessmentScore());
        session.setSelfAssessmentNote(dto.getSelfAssessmentNote());
        return practiceSessionRepository.save(session);
    }

    @Transactional
    public PracticeSession saveOrUpdate(PracticeSessionDTO dto) {
        PracticeSession session;
        if (dto.getId() != null) {
            session = practiceSessionRepository.findById(dto.getId()).orElse(new PracticeSession());
        } else {
            session = new PracticeSession();
            session.setUserId(dto.getUserId());
            session.setManuscriptId(dto.getManuscriptId());
            session.setStartTime(dto.getStartTime());
            session.setSessionDate(dto.getStartTime().toLocalDate());
        }
        if (dto.getEndTime() != null) {
            session.setEndTime(dto.getEndTime());
            if (session.getStartTime() != null) {
                session.setDurationSeconds((int) Duration.between(session.getStartTime(), dto.getEndTime()).getSeconds());
            }
        }
        if (dto.getDurationSeconds() != null) {
            session.setDurationSeconds(dto.getDurationSeconds());
        }
        if (dto.getCompletedParagraphs() != null) {
            session.setCompletedParagraphs(dto.getCompletedParagraphs());
        }
        if (dto.getSelfAssessmentStatus() != null) {
            session.setSelfAssessmentStatus(dto.getSelfAssessmentStatus());
        }
        if (dto.getSelfAssessmentScore() != null) {
            session.setSelfAssessmentScore(dto.getSelfAssessmentScore());
        }
        if (dto.getSelfAssessmentNote() != null) {
            session.setSelfAssessmentNote(dto.getSelfAssessmentNote());
        }
        if (session.getSessionDate() == null && session.getStartTime() != null) {
            session.setSessionDate(session.getStartTime().toLocalDate());
        }
        return practiceSessionRepository.save(session);
    }

    public List<PracticeSession> getByUserAndManuscript(Long userId, Long manuscriptId) {
        return practiceSessionRepository.findByUserIdAndManuscriptIdOrderByStartTimeDesc(userId, manuscriptId);
    }

    public List<PracticeSession> getByUserAndDate(Long userId, LocalDate date) {
        return practiceSessionRepository.findByUserIdAndSessionDateOrderByStartTimeDesc(userId, date);
    }

    public List<PracticeSession> getByUser(Long userId) {
        return practiceSessionRepository.findByUserIdOrderByStartTimeDesc(userId);
    }

    public PracticeSession getLatestSession(Long userId, Long manuscriptId) {
        return practiceSessionRepository.findFirstByUserIdAndManuscriptIdOrderByStartTimeDesc(userId, manuscriptId).orElse(null);
    }

    public Map<String, Object> getSessionStats(Long userId, Long manuscriptId) {
        Map<String, Object> stats = new HashMap<>();
        Long totalDuration = practiceSessionRepository.sumDurationByUserAndManuscript(userId, manuscriptId);
        Long sessionCount = practiceSessionRepository.countSessionsByUserAndManuscript(userId, manuscriptId);
        java.time.LocalDateTime lastEndTime = practiceSessionRepository.findLastEndTimeByUserAndManuscript(userId, manuscriptId);

        stats.put("totalDurationSeconds", totalDuration != null ? totalDuration : 0);
        stats.put("sessionCount", sessionCount != null ? sessionCount : 0);
        stats.put("lastPracticeTime", lastEndTime);

        List<PracticeSession> sessions = getByUserAndManuscript(userId, manuscriptId);
        int completedSessions = 0;
        int totalScore = 0;
        for (PracticeSession s : sessions) {
            if (s.getEndTime() != null || s.getDurationSeconds() != null) {
                completedSessions++;
            }
            if (s.getSelfAssessmentScore() != null) {
                totalScore += s.getSelfAssessmentScore();
            }
        }
        stats.put("completedSessionCount", completedSessions);
        stats.put("averageScore", completedSessions > 0 && totalScore > 0 ? (double) totalScore / completedSessions : 0);

        return stats;
    }

    public List<PracticeSession> getByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return practiceSessionRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Transactional
    public boolean deleteSession(Long sessionId, Long userId) {
        PracticeSession session = practiceSessionRepository.findById(sessionId).orElse(null);
        if (session == null || !session.getUserId().equals(userId)) {
            return false;
        }
        practiceSessionRepository.delete(session);
        return true;
    }
}
