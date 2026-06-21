CREATE DATABASE IF NOT EXISTS recitation_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE recitation_db;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    genre VARCHAR(100),
    target_audience VARCHAR(100),
    training_focus VARCHAR(200),
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

INSERT INTO category (name, description, genre, target_audience, training_focus, sort_order) VALUES
('现代文', '现代散文、小说选段等现代文学作品', '散文、小说、记叙文', '青少年、成人', '语感培养、理解表达、情感把控', 1),
('古诗词', '唐诗、宋词、元曲等古典诗词', '古诗、词、曲、赋', '青少年、成人、古诗词爱好者', '节奏感、韵律感、意境理解、古汉语发音', 2),
('寓言故事', '寓言、童话、小故事等', '寓言、童话、短篇故事', '儿童、青少年', '语速控制、角色塑造、童趣表达', 3),
('诗歌', '现代诗歌、散文诗等', '现代诗、散文诗', '青少年、成人、诗歌爱好者', '情感表达、节奏感、意象传递', 4),
('散文', '抒情散文、叙事散文等', '抒情散文、叙事散文、议论散文', '青少年、成人', '气息控制、情感层次、语言节奏', 5),
('经典名著', '中外经典名著选段', '小说、戏剧、名著选段', '青少年、成人、文学爱好者', '人物塑造、场景描绘、情感深度', 6),
('演讲文稿', '演讲稿、致辞稿等', '演讲稿、致辞、发言稿', '成人、职场人士、学生', '感染力、逻辑重音、停顿技巧、气场把控', 7),
('儿童文学', '适合儿童朗读的文学作品', '童话、儿歌、儿童故事', '儿童、亲子阅读', '咬字清晰、语速适中、情感纯真、感染力', 8);
