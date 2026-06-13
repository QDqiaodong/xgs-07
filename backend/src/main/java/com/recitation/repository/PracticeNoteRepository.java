package com.recitation.repository;

import com.recitation.entity.PracticeNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeNoteRepository extends JpaRepository<PracticeNote, Long> {

    Optional<PracticeNote> findByUserIdAndManuscriptId(Long userId, Long manuscriptId);

    Page<PracticeNote> findByUserIdOrderByUpdateTimeDesc(Long userId, Pageable pageable);

    List<PracticeNote> findByManuscriptIdOrderByCreateTimeDesc(Long manuscriptId);
}
