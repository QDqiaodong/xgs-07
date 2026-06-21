ALTER TABLE manuscript ADD COLUMN training_tags VARCHAR(200) DEFAULT NULL COMMENT '朗读训练标签：换气,重音,节奏,情绪递进' AFTER difficulty;

CREATE INDEX idx_training_tags ON manuscript(training_tags);
