package com.recitation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "paragraph_progress", indexes = {
        @Index(name = "idx_user_manuscript", columnList = "userId, manuscriptId")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_manuscript_paragraph", columnNames = {"userId", "manuscriptId", "paragraphIndex"})
})
public class ParagraphProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long manuscriptId;

    @Column(nullable = false)
    private Integer paragraphIndex;

    @Column(length = 20)
    private String status;

    @Column(nullable = false)
    private Integer practiceCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
