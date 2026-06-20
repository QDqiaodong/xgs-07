package com.recitation.repository;

import com.recitation.entity.PracticeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {

    List<PracticeSession> findByUserIdAndManuscriptIdOrderByStartTimeDesc(Long userId, Long manuscriptId);

    List<PracticeSession> findByUserIdAndSessionDateOrderByStartTimeDesc(Long userId, LocalDate sessionDate);

    List<PracticeSession> findByUserIdOrderByStartTimeDesc(Long userId);

    Optional<PracticeSession> findFirstByUserIdAndManuscriptIdOrderByStartTimeDesc(Long userId, Long manuscriptId);

    @Query("SELECT COALESCE(SUM(ps.durationSeconds), 0) FROM PracticeSession ps WHERE ps.userId = :userId AND ps.manuscriptId = :manuscriptId")
    Long sumDurationByUserAndManuscript(@Param("userId") Long userId, @Param("manuscriptId") Long manuscriptId);

    @Query("SELECT COUNT(ps) FROM PracticeSession ps WHERE ps.userId = :userId AND ps.manuscriptId = :manuscriptId")
    Long countSessionsByUserAndManuscript(@Param("userId") Long userId, @Param("manuscriptId") Long manuscriptId);

    @Query("SELECT MAX(ps.endTime) FROM PracticeSession ps WHERE ps.userId = :userId AND ps.manuscriptId = :manuscriptId")
    java.time.LocalDateTime findLastEndTimeByUserAndManuscript(@Param("userId") Long userId, @Param("manuscriptId") Long manuscriptId);

    @Query("SELECT ps FROM PracticeSession ps WHERE ps.userId = :userId AND ps.sessionDate BETWEEN :startDate AND :endDate ORDER BY ps.sessionDate ASC")
    List<PracticeSession> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
}
