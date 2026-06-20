CREATE TABLE IF NOT EXISTS manuscript_paragraph (
    id BIGINT NOT NULL AUTO_INCREMENT,
    manuscript_id BIGINT NOT NULL,
    paragraph_index INT NOT NULL,
    content TEXT NOT NULL,
    reading_tip TEXT,
    practice_focus TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_manuscript_paragraph (manuscript_id, paragraph_index),
    INDEX idx_manuscript (manuscript_id)
);
