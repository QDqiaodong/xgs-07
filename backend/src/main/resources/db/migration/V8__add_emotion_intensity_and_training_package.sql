-- 为 emotion_band 增加情绪强度字段（0-100，用于情绪曲线）
ALTER TABLE emotion_band ADD COLUMN emotion_intensity INT DEFAULT 50 COMMENT '情绪强度 0-100';

-- 训练包表
CREATE TABLE IF NOT EXISTS training_package (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '训练包名称',
    description VARCHAR(500) COMMENT '训练包描述',
    cover_image VARCHAR(255) COMMENT '封面图片URL',
    category_id BIGINT COMMENT '分类ID',
    category_name VARCHAR(50) COMMENT '分类名称',
    difficulty VARCHAR(50) COMMENT '难度等级',
    target_duration INT DEFAULT 0 COMMENT '目标练习时长（分钟）',
    target_days INT DEFAULT 0 COMMENT '目标完成天数',
    training_goals TEXT COMMENT '训练目标，JSON数组格式',
    tips TEXT COMMENT '训练提示',
    sort_order INT NOT NULL DEFAULT 0,
    status BIT(1) NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    is_public BIT(1) NOT NULL DEFAULT 1 COMMENT '是否公开',
    create_user VARCHAR(50) COMMENT '创建用户',
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_category (category_id),
    INDEX idx_difficulty (difficulty),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文稿训练包';

-- 训练包-文稿关联表
CREATE TABLE IF NOT EXISTS training_package_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    package_id BIGINT NOT NULL COMMENT '训练包ID',
    manuscript_id BIGINT NOT NULL COMMENT '文稿ID',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    stage VARCHAR(50) COMMENT '阶段：铺垫/发展/高潮/收束',
    stage_note VARCHAR(255) COMMENT '阶段说明',
    create_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_package_manuscript (package_id, manuscript_id),
    INDEX idx_package (package_id),
    INDEX idx_manuscript (manuscript_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练包文稿项';

-- 用户训练包进度表
CREATE TABLE IF NOT EXISTS training_package_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    package_id BIGINT NOT NULL COMMENT '训练包ID',
    status VARCHAR(20) DEFAULT 'not_started' COMMENT '状态：not_started/in_progress/completed',
    start_date DATE COMMENT '开始日期',
    complete_date DATE COMMENT '完成日期',
    current_manuscript_index INT DEFAULT 0 COMMENT '当前练习到第几个文稿',
    total_practice_minutes INT DEFAULT 0 COMMENT '累计练习分钟数',
    total_practice_count INT DEFAULT 0 COMMENT '累计练习次数',
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_package (user_id, package_id),
    INDEX idx_user (user_id),
    INDEX idx_package (package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户训练包进度';
