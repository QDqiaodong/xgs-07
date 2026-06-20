ALTER TABLE practice_note
    MODIFY COLUMN emotion_control_score DOUBLE DEFAULT NULL COMMENT '情绪控制评分 1-10分，支持半星小数';

DELETE eb
FROM emotion_band eb
JOIN (
    SELECT user_id, manuscript_id, paragraph_index, MAX(id) AS keep_id
    FROM emotion_band
    GROUP BY user_id, manuscript_id, paragraph_index
    HAVING COUNT(*) > 1
) duplicated
  ON eb.user_id = duplicated.user_id
 AND eb.manuscript_id = duplicated.manuscript_id
 AND eb.paragraph_index = duplicated.paragraph_index
WHERE eb.id <> duplicated.keep_id;

SET @emotion_band_unique_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'emotion_band'
      AND index_name = 'uk_user_manuscript_paragraph'
);

SET @emotion_band_unique_sql = IF(
    @emotion_band_unique_exists = 0,
    'ALTER TABLE emotion_band ADD UNIQUE INDEX uk_user_manuscript_paragraph (user_id, manuscript_id, paragraph_index)',
    'SELECT 1'
);

PREPARE emotion_band_unique_stmt FROM @emotion_band_unique_sql;
EXECUTE emotion_band_unique_stmt;
DEALLOCATE PREPARE emotion_band_unique_stmt;
