package com.recitation.repository;

import com.recitation.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByStatusTrueOrderBySortOrderAsc();
    Optional<Category> findByIdAndStatusTrue(Long id);
}
