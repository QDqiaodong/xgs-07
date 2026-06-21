package com.recitation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "training_package_item", indexes = {
        @Index(name = "idx_package", columnList = "packageId"),
        @Index(name = "idx_manuscript", columnList = "manuscriptId")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_package_manuscript", columnNames = {"packageId", "manuscriptId"})
})
public class TrainingPackageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long packageId;

    @Column(nullable = false)
    private Long manuscriptId;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(length = 50)
    private String stage;

    @Column(length = 255)
    private String stageNote;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;
}
