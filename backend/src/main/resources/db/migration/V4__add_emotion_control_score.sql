ALTER TABLE practice_note
    ADD COLUMN emotion_control_score INT DEFAULT NULL COMMENT '情绪控制评分 1-10分',
    ADD COLUMN emotion_control_note TEXT DEFAULT NULL COMMENT '情绪控制说明';
