package com.recitation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "manuscript", indexes = {
        @Index(name = "idx_category", columnList = "categoryId"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_author", columnList = "author")
})
public class Manuscript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String introduction;

    @Column(nullable = false)
    private Long categoryId;

    @Column(length = 50)
    private String categoryName;

    @Column(length = 50)
    private String author;

    @Column(length = 50)
    private String difficulty;

    @Column(length = 200)
    private String trainingTags;

    @Column(nullable = false)
    private Boolean isPublic = false;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private Integer favoriteCount = 0;

    @Column(length = 50)
    private String createUser;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
