package com.recitation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "training_package_progress", indexes = {
        @Index(name = "idx_user", columnList = "userId"),
        @Index(name = "idx_package", columnList = "packageId")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_package", columnNames = {"userId", "packageId"})
})
public class TrainingPackageProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long packageId;

    @Column(length = 20)
    private String status = "not_started";

    private LocalDate startDate;

    private LocalDate completeDate;

    @Column
    private Integer currentManuscriptIndex = 0;

    @Column
    private Integer totalPracticeMinutes = 0;

    @Column
    private Integer totalPracticeCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
