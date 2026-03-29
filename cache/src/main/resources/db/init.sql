-- 创建数据库
CREATE
DATABASE IF NOT EXISTS cache_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE
cache_demo;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`
    BIGINT
    NOT
    NULL
    AUTO_INCREMENT
    COMMENT
    '用户ID',
    `username`
    VARCHAR
(
    50
) NOT NULL COMMENT '用户名',
    `email` VARCHAR
(
    100
) COMMENT '邮箱',
    `age` INT COMMENT '年龄',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY
(
    `id`
),
    UNIQUE KEY `uk_username`
(
    `username`
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入测试数据
INSERT INTO `user` (`username`, `email`, `age`)
VALUES ('张三', 'zhangsan@example.com', 25),
       ('李四', 'lisi@example.com', 30),
       ('王五', 'wangwu@example.com', 28);