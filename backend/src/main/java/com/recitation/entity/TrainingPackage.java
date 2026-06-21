package com.recitation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "training_package", indexes = {
        @Index(name = "idx_category", columnList = "categoryId"),
        @Index(name = "idx_difficulty", columnList = "difficulty"),
        @Index(name = "idx_status", columnList = "status")
})
public class TrainingPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String coverImage;

    private Long categoryId;

    @Column(length = 50)
    private String categoryName;

    @Column(length = 50)
    private String difficulty;

    @Column
    private Integer targetDuration = 0;

    @Column
    private Integer targetDays = 0;

    @Column(columnDefinition = "TEXT")
    private String trainingGoals;

    @Column(columnDefinition = "TEXT")
    private String tips;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean status = true;

    @Column(nullable = false)
    private Boolean isPublic = true;

    @Column(length = 50)
    private String createUser;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
