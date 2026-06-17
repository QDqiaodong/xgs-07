package com.recitation.repository;

import com.recitation.entity.ParagraphProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParagraphProgressRepository extends JpaRepository<ParagraphProgress, Long> {

    List<ParagraphProgress> findByUserIdAndManuscriptId(Long userId, Long manuscriptId);

    Optional<ParagraphProgress> findByUserIdAndManuscriptIdAndParagraphIndex(Long userId, Long manuscriptId, Integer paragraphIndex);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "INSERT INTO paragraph_progress (user_id, manuscript_id, paragraph_index, status, create_time, update_time) " +
            "VALUES (:userId, :manuscriptId, :paragraphIndex, :status, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE status = VALUES(status), update_time = NOW()",
            nativeQuery = true)
    void upsertByUniqueKey(@Param("userId") Long userId,
                           @Param("manuscriptId") Long manuscriptId,
                           @Param("paragraphIndex") Integer paragraphIndex,
                           @Param("status") String status);

    @Modifying
    @Transactional
    void deleteByUserIdAndManuscriptIdAndParagraphIndex(Long userId, Long manuscriptId, Integer paragraphIndex);

    @Modifying
    @Transactional
    void deleteByManuscriptId(Long manuscriptId);
}
