package com.recitation.repository;

import com.recitation.entity.TrainingPackageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingPackageItemRepository extends JpaRepository<TrainingPackageItem, Long> {

    List<TrainingPackageItem> findByPackageIdOrderBySortOrderAsc(Long packageId);

    void deleteByPackageId(Long packageId);
}
