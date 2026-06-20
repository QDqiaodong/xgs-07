package com.recitation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "practice_session", indexes = {
        @Index(name = "idx_user_manuscript", columnList = "userId, manuscriptId"),
        @Index(name = "idx_user_date", columnList = "userId, sessionDate")
})
public class PracticeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long manuscriptId;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer durationSeconds;

    @Column(columnDefinition = "TEXT")
    private String completedParagraphs;

    @Column(length = 20)
    private String selfAssessmentStatus;

    private Integer selfAssessmentScore;

    @Column(columnDefinition = "TEXT")
    private String selfAssessmentNote;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
