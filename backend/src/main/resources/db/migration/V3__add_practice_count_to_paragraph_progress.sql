SET @practice_count_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'paragraph_progress'
      AND column_name = 'practice_count'
);

SET @practice_count_sql = IF(
    @practice_count_exists = 0,
    'ALTER TABLE paragraph_progress ADD COLUMN practice_count INT NOT NULL DEFAULT 0 AFTER status',
    'SELECT 1'
);

PREPARE practice_count_stmt FROM @practice_count_sql;
EXECUTE practice_count_stmt;
DEALLOCATE PREPARE practice_count_stmt;
