package com.recitation.repository;

import com.recitation.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndManuscriptId(Long userId, Long manuscriptId);

    Page<Favorite> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    List<Favorite> findByUserId(Long userId);

    boolean existsByUserIdAndManuscriptId(Long userId, Long manuscriptId);

    void deleteByUserIdAndManuscriptId(Long userId, Long manuscriptId);

    @Modifying
    @Transactional
    void deleteByManuscriptId(Long manuscriptId);
}
