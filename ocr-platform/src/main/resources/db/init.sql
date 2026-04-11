-- 创建数据库
CREATE DATABASE IF NOT EXISTS ocr_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ocr_db;

-- OCR识别记录表
CREATE TABLE IF NOT EXISTS ocr_record (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          task_id VARCHAR(64) NOT NULL UNIQUE COMMENT '任务ID',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    file_type VARCHAR(100) COMMENT '文件MIME类型',
    recognized_text LONGTEXT COMMENT '识别出的文本内容',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/SUCCESS/FAILED',
    error_message TEXT COMMENT '错误信息',
    recognition_duration INT COMMENT '识别耗时（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_id (task_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FULLTEXT INDEX ft_recognized_text (recognized_text)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OCR识别记录表';