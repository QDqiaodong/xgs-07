CREATE TABLE IF NOT EXISTS pronunciation_difficulty (
    id BIGINT NOT NULL AUTO_INCREMENT,
    manuscript_id BIGINT NOT NULL,
    paragraph_index INT NOT NULL,
    user_id BIGINT,
    is_public TINYINT(1) NOT NULL DEFAULT 0,
    polyphonic_words TEXT,
    linking TEXT,
    stress TEXT,
    breath_points TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_manuscript_paragraph_user (manuscript_id, paragraph_index, user_id),
    INDEX idx_manuscript (manuscript_id),
    INDEX idx_user (user_id)
);

CREATE TABLE IF NOT EXISTS practice_session (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    manuscript_id BIGINT NOT NULL,
    session_date DATE NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    duration_seconds INT,
    completed_paragraphs TEXT,
    self_assessment_status VARCHAR(20),
    self_assessment_score INT,
    self_assessment_note TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_user_manuscript (user_id, manuscript_id),
    INDEX idx_user_date (user_id, session_date)
);
