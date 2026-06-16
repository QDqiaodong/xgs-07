CREATE TABLE IF NOT EXISTS category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    sort_order INT NOT NULL DEFAULT 0,
    status BIT(1) NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS manuscript (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    introduction VARCHAR(500),
    category_id BIGINT NOT NULL,
    category_name VARCHAR(50),
    author VARCHAR(50),
    difficulty VARCHAR(50),
    is_public BIT(1) NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    view_count INT NOT NULL DEFAULT 0,
    favorite_count INT NOT NULL DEFAULT 0,
    create_user VARCHAR(50),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_author (author)
);

CREATE TABLE IF NOT EXISTS paragraph_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    manuscript_id BIGINT NOT NULL,
    paragraph_index INT NOT NULL,
    status VARCHAR(20),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_manuscript_paragraph (user_id, manuscript_id, paragraph_index),
    INDEX idx_user_manuscript (user_id, manuscript_id)
);

CREATE TABLE IF NOT EXISTS practice_note (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    manuscript_id BIGINT NOT NULL,
    difficulty_points TEXT,
    tone_control TEXT,
    emotion_expression TEXT,
    other_notes TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_manuscript (user_id, manuscript_id),
    INDEX idx_manuscript (manuscript_id),
    INDEX idx_user (user_id)
);

CREATE TABLE IF NOT EXISTS emotion_band (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    manuscript_id BIGINT NOT NULL,
    paragraph_index INT NOT NULL,
    emotion_type VARCHAR(30),
    remark VARCHAR(200),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_emotion_user_manuscript (user_id, manuscript_id)
);

CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    manuscript_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_manuscript (user_id, manuscript_id)
);
