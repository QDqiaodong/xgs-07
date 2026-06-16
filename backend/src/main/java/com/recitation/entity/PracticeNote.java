package com.recitation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "practice_note", indexes = {
        @Index(name = "idx_manuscript", columnList = "manuscriptId"),
        @Index(name = "idx_user", columnList = "userId")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_manuscript", columnNames = {"userId", "manuscriptId"})
})
public class PracticeNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long manuscriptId;

    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String difficultyPoints;

    @Column(columnDefinition = "TEXT")
    private String toneControl;

    @Column(columnDefinition = "TEXT")
    private String emotionExpression;

    @Column(columnDefinition = "TEXT")
    private String otherNotes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
