package com.recitation.repository;

import com.recitation.entity.Manuscript;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManuscriptRepository extends JpaRepository<Manuscript, Long> {

    Page<Manuscript> findByIsPublicTrueAndStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);

    Page<Manuscript> findByIsPublicTrueAndStatusAndCategoryIdOrderByCreateTimeDesc(Integer status, Long categoryId, Pageable pageable);

    List<Manuscript> findTop10ByIsPublicTrueAndStatusOrderByViewCountDesc(Integer status);

    List<Manuscript> findTop10ByIsPublicTrueAndStatusOrderByFavoriteCountDesc(Integer status);

    List<Manuscript> findByAuthorAndIsPublicTrueAndStatus(String author, Integer status);

    List<Manuscript> findByIsPublicTrueAndStatus(Integer status);

    @Modifying
    @Query("UPDATE Manuscript m SET m.viewCount = m.viewCount + 1 WHERE m.id = :id")
    int incrementViewCount(Long id);

    @Modifying
    @Query("UPDATE Manuscript m SET m.favoriteCount = m.favoriteCount + 1 WHERE m.id = :id")
    int incrementFavoriteCount(Long id);

    @Modifying
    @Query("UPDATE Manuscript m SET m.favoriteCount = m.favoriteCount - 1 WHERE m.id = :id")
    int decrementFavoriteCount(Long id);
}
