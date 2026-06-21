-- 为分类表增加体裁、适合人群、训练重点字段
ALTER TABLE category ADD COLUMN genre VARCHAR(100) COMMENT '体裁，如：诗歌、散文、演讲稿、小说等';
ALTER TABLE category ADD COLUMN target_audience VARCHAR(100) COMMENT '适合人群，如：儿童、青少年、成人、专业朗读者等';
ALTER TABLE category ADD COLUMN training_focus VARCHAR(200) COMMENT '训练重点，如：节奏感、情感表达、吐字清晰等';
