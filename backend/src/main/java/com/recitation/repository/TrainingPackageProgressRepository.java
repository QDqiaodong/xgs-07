package com.recitation.repository;

import com.recitation.entity.TrainingPackageProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingPackageProgressRepository extends JpaRepository<TrainingPackageProgress, Long> {

    Optional<TrainingPackageProgress> findByUserIdAndPackageId(Long userId, Long packageId);

    List<TrainingPackageProgress> findByUserId(Long userId);

    List<TrainingPackageProgress> findByUserIdAndStatus(Long userId, String status);

    List<TrainingPackageProgress> findByPackageId(Long packageId);
}
