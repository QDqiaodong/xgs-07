package com.recitation.repository;

import com.recitation.entity.ManuscriptParagraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManuscriptParagraphRepository extends JpaRepository<ManuscriptParagraph, Long> {

    List<ManuscriptParagraph> findByManuscriptIdOrderByParagraphIndexAsc(Long manuscriptId);

    Optional<ManuscriptParagraph> findByManuscriptIdAndParagraphIndex(Long manuscriptId, Integer paragraphIndex);

    @Query("SELECT MAX(p.updateTime) FROM ManuscriptParagraph p WHERE p.manuscriptId = :manuscriptId")
    java.time.LocalDateTime findLastUpdateTimeByManuscript(@Param("manuscriptId") Long manuscriptId);

    @Modifying
    @Transactional
    void deleteByManuscriptId(Long manuscriptId);
}
