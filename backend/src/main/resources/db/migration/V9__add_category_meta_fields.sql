-- 为分类表增加体裁、适合人群、训练重点字段
DELIMITER //

CREATE PROCEDURE add_category_meta_columns()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'category'
          AND COLUMN_NAME = 'genre'
    ) THEN
        ALTER TABLE category ADD COLUMN genre VARCHAR(100) COMMENT '体裁，如：诗歌、散文、演讲稿、小说等';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'category'
          AND COLUMN_NAME = 'target_audience'
    ) THEN
        ALTER TABLE category ADD COLUMN target_audience VARCHAR(100) COMMENT '适合人群，如：儿童、青少年、成人、专业朗读者等';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'category'
          AND COLUMN_NAME = 'training_focus'
    ) THEN
        ALTER TABLE category ADD COLUMN training_focus VARCHAR(200) COMMENT '训练重点，如：节奏感、情感表达、吐字清晰等';
    END IF;
END//

CALL add_category_meta_columns()//

DROP PROCEDURE add_category_meta_columns//

DELIMITER ;
