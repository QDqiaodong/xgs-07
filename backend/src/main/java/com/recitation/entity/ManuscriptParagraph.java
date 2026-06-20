package com.recitation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "manuscript_paragraph", indexes = {
        @Index(name = "idx_manuscript", columnList = "manuscriptId")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_manuscript_paragraph", columnNames = {"manuscriptId", "paragraphIndex"})
})
public class ManuscriptParagraph {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long manuscriptId;

    @Column(nullable = false)
    private Integer paragraphIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String readingTip;

    @Column(columnDefinition = "TEXT")
    private String practiceFocus;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
