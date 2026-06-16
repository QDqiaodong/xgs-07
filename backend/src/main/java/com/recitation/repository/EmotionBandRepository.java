package com.recitation.repository;

import com.recitation.entity.EmotionBand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionBandRepository extends JpaRepository<EmotionBand, Long> {

    List<EmotionBand> findByUserIdAndManuscriptId(Long userId, Long manuscriptId);

    Optional<EmotionBand> findByUserIdAndManuscriptIdAndParagraphIndex(Long userId, Long manuscriptId, Integer paragraphIndex);

    void deleteByUserIdAndManuscriptIdAndParagraphIndex(Long userId, Long manuscriptId, Integer paragraphIndex);
}
