package com.recitation.repository;

import com.recitation.entity.PracticeNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeNoteRepository extends JpaRepository<PracticeNote, Long> {

    Optional<PracticeNote> findByUserIdAndManuscriptId(Long userId, Long manuscriptId);

    Page<PracticeNote> findByUserIdOrderByUpdateTimeDesc(Long userId, Pageable pageable);

    List<PracticeNote> findByUserIdOrderByUpdateTimeDesc(Long userId);

    List<PracticeNote> findByManuscriptIdOrderByCreateTimeDesc(Long manuscriptId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "INSERT INTO practice_note (user_id, manuscript_id, difficulty_points, tone_control, emotion_expression, other_notes, create_time, update_time) " +
            "VALUES (:userId, :manuscriptId, :difficultyPoints, :toneControl, :emotionExpression, :otherNotes, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE difficulty_points = VALUES(difficulty_points), tone_control = VALUES(tone_control), " +
            "emotion_expression = VALUES(emotion_expression), other_notes = VALUES(other_notes), update_time = NOW()",
            nativeQuery = true)
    void upsertByUniqueKey(@Param("userId") Long userId,
                           @Param("manuscriptId") Long manuscriptId,
                           @Param("difficultyPoints") String difficultyPoints,
                           @Param("toneControl") String toneControl,
                           @Param("emotionExpression") String emotionExpression,
                           @Param("otherNotes") String otherNotes);

    @Modifying
    @Transactional
    void deleteByUserIdAndManuscriptId(Long userId, Long manuscriptId);

    @Modifying
    @Transactional
    void deleteByManuscriptId(Long manuscriptId);
}
