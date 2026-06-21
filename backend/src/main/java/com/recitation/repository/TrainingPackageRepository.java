package com.recitation.repository;

import com.recitation.entity.TrainingPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingPackageRepository extends JpaRepository<TrainingPackage, Long> {

    List<TrainingPackage> findByStatusTrueAndIsPublicTrueOrderBySortOrderAscCreateTimeDesc();

    List<TrainingPackage> findByCategoryIdAndStatusTrueAndIsPublicTrueOrderBySortOrderAsc(Long categoryId);

    List<TrainingPackage> findByDifficultyAndStatusTrueAndIsPublicTrueOrderBySortOrderAsc(String difficulty);

    List<TrainingPackage> findByCreateUserAndStatusTrueOrderBySortOrderAsc(String createUser);
}
