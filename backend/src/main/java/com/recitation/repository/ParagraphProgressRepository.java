package com.recitation.repository;

import com.recitation.entity.ParagraphProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParagraphProgressRepository extends JpaRepository<ParagraphProgress, Long> {

    List<ParagraphProgress> findByUserIdAndManuscriptId(Long userId, Long manuscriptId);

    Optional<ParagraphProgress> findByUserIdAndManuscriptIdAndParagraphIndex(Long userId, Long manuscriptId, Integer paragraphIndex);

    void deleteByUserIdAndManuscriptIdAndParagraphIndex(Long userId, Long manuscriptId, Integer paragraphIndex);
}
