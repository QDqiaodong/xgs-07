CREATE DATABASE IF NOT EXISTS recitation_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE recitation_db;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    sort_order INT NOT NULL DEFAULT 0,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS manuscript (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    introduction VARCHAR(500),
    category_id BIGINT NOT NULL,
    category_name VARCHAR(50),
    author VARCHAR(50),
    difficulty VARCHAR(50),
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    status INT NOT NULL DEFAULT 1,
    view_count INT NOT NULL DEFAULT 0,
    favorite_count INT NOT NULL DEFAULT 0,
    create_user VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_author (author)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    manuscript_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_manuscript (user_id, manuscript_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS practice_note (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    manuscript_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    difficulty_points TEXT,
    tone_control TEXT,
    emotion_expression TEXT,
    other_notes TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_manuscript (user_id, manuscript_id),
    INDEX idx_manuscript (manuscript_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS paragraph_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    manuscript_id BIGINT NOT NULL,
    paragraph_index INT NOT NULL,
    status VARCHAR(20),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_manuscript_paragraph (user_id, manuscript_id, paragraph_index),
    INDEX idx_user_manuscript (user_id, manuscript_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS emotion_band (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    manuscript_id BIGINT NOT NULL,
    paragraph_index INT NOT NULL,
    emotion_type VARCHAR(30),
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_emotion_user_manuscript (user_id, manuscript_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS manuscript_paragraph (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    manuscript_id BIGINT NOT NULL,
    paragraph_index INT NOT NULL,
    content TEXT NOT NULL,
    reading_tip TEXT,
    practice_focus TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_manuscript_paragraph (manuscript_id, paragraph_index),
    INDEX idx_manuscript (manuscript_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO category (name, description, sort_order) VALUES
('现代文', '现代散文、小说选段等现代文学作品', 1),
('古诗词', '唐诗、宋词、元曲等古典诗词', 2),
('寓言故事', '寓言、童话、小故事等', 3),
('诗歌', '现代诗歌、散文诗等', 4),
('散文', '抒情散文、叙事散文等', 5),
('经典名著', '中外经典名著选段', 6),
('演讲文稿', '演讲稿、致辞稿等', 7),
('儿童文学', '适合儿童朗读的文学作品', 8);
