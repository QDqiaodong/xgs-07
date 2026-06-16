package com.recitation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "emotion_band", indexes = {
        @Index(name = "idx_emotion_user_manuscript", columnList = "userId, manuscriptId")
})
public class EmotionBand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long manuscriptId;

    @Column(nullable = false)
    private Integer paragraphIndex;

    @Column(length = 30)
    private String emotionType;

    @Column(length = 200)
    private String remark;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
