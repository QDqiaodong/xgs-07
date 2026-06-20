package com.recitation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pronunciation_difficulty", indexes = {
        @Index(name = "idx_manuscript", columnList = "manuscriptId"),
        @Index(name = "idx_user", columnList = "userId")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_manuscript_paragraph_user", columnNames = {"manuscriptId", "paragraphIndex", "userId"})
})
public class PronunciationDifficulty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long manuscriptId;

    @Column(nullable = false)
    private Integer paragraphIndex;

    private Long userId;

    @Column(nullable = false)
    private Boolean isPublic = false;

    @Column(columnDefinition = "TEXT")
    private String polyphonicWords;

    @Column(columnDefinition = "TEXT")
    private String linking;

    @Column(columnDefinition = "TEXT")
    private String stress;

    @Column(columnDefinition = "TEXT")
    private String breathPoints;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
