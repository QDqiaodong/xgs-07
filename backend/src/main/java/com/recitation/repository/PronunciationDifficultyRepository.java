package com.recitation.repository;

import com.recitation.entity.PronunciationDifficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PronunciationDifficultyRepository extends JpaRepository<PronunciationDifficulty, Long> {

    List<PronunciationDifficulty> findByManuscriptId(Long manuscriptId);

    List<PronunciationDifficulty> findByManuscriptIdAndUserIdOrIsPublicTrue(Long manuscriptId, Long userId);

    Optional<PronunciationDifficulty> findByManuscriptIdAndParagraphIndexAndUserId(Long manuscriptId, Integer paragraphIndex, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "INSERT INTO pronunciation_difficulty (manuscript_id, paragraph_index, user_id, is_public, polyphonic_words, linking, stress, breath_points, create_time, update_time) " +
            "VALUES (:manuscriptId, :paragraphIndex, :userId, :isPublic, :polyphonicWords, :linking, :stress, :breathPoints, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "is_public = VALUES(is_public), " +
            "polyphonic_words = VALUES(polyphonic_words), " +
            "linking = VALUES(linking), " +
            "stress = VALUES(stress), " +
            "breath_points = VALUES(breath_points), " +
            "update_time = NOW()",
            nativeQuery = true)
    void upsertByUniqueKey(@Param("manuscriptId") Long manuscriptId,
                           @Param("paragraphIndex") Integer paragraphIndex,
                           @Param("userId") Long userId,
                           @Param("isPublic") Boolean isPublic,
                           @Param("polyphonicWords") String polyphonicWords,
                           @Param("linking") String linking,
                           @Param("stress") String stress,
                           @Param("breathPoints") String breathPoints);

    @Modifying
    @Transactional
    void deleteByManuscriptIdAndParagraphIndexAndUserId(Long manuscriptId, Integer paragraphIndex, Long userId);

    @Modifying
    @Transactional
    void deleteByManuscriptId(Long manuscriptId);
}
