DELETE pp
FROM paragraph_progress pp
JOIN (
    SELECT user_id, manuscript_id, paragraph_index, MAX(id) AS keep_id
    FROM paragraph_progress
    GROUP BY user_id, manuscript_id, paragraph_index
    HAVING COUNT(*) > 1
) duplicated
  ON pp.user_id = duplicated.user_id
 AND pp.manuscript_id = duplicated.manuscript_id
 AND pp.paragraph_index = duplicated.paragraph_index
WHERE pp.id <> duplicated.keep_id;

DELETE pn
FROM practice_note pn
JOIN (
    SELECT user_id, manuscript_id, MAX(id) AS keep_id
    FROM practice_note
    GROUP BY user_id, manuscript_id
    HAVING COUNT(*) > 1
) duplicated
  ON pn.user_id = duplicated.user_id
 AND pn.manuscript_id = duplicated.manuscript_id
WHERE pn.id <> duplicated.keep_id;

SET @paragraph_progress_unique_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'paragraph_progress'
      AND index_name = 'uk_user_manuscript_paragraph'
);

SET @paragraph_progress_unique_sql = IF(
    @paragraph_progress_unique_exists = 0,
    'ALTER TABLE paragraph_progress ADD UNIQUE INDEX uk_user_manuscript_paragraph (user_id, manuscript_id, paragraph_index)',
    'SELECT 1'
);

PREPARE paragraph_progress_unique_stmt FROM @paragraph_progress_unique_sql;
EXECUTE paragraph_progress_unique_stmt;
DEALLOCATE PREPARE paragraph_progress_unique_stmt;

SET @practice_note_unique_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'practice_note'
      AND index_name = 'uk_user_manuscript'
);

SET @practice_note_unique_sql = IF(
    @practice_note_unique_exists = 0,
    'ALTER TABLE practice_note ADD UNIQUE INDEX uk_user_manuscript (user_id, manuscript_id)',
    'SELECT 1'
);

PREPARE practice_note_unique_stmt FROM @practice_note_unique_sql;
EXECUTE practice_note_unique_stmt;
DEALLOCATE PREPARE practice_note_unique_stmt;
