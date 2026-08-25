-- =====================================================================
-- 消息通知重构迁移脚本（在本地 MySQL 库上执行一次）
-- 1) sys_notifications：新增推送对象 notice_push(json)，删除 is_read
-- 2) 新建 sys_notifications_receiver（每接收人状态：is_read 管理端可改 / is_deleted 接收人删除）
-- 3) 存量已读状态迁移到 receiver（必须在删除 is_read 之前执行）
-- 4) 存量单接收人数据回填 notice_push（避免重构后旧数据变成全员可见）
-- =====================================================================

-- 1) sys_notifications：先加 notice_push（暂不删 is_read，稍后迁移已读状态）
ALTER TABLE `sys_notifications`
  ADD COLUMN `notice_push` json NULL COMMENT '推送对象 JSON：{"user":[],"dept":[],"role":[]}' AFTER `user_id`;

-- 2) 新建 receiver 表
CREATE TABLE IF NOT EXISTS `sys_notifications_receiver` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `notification_id` bigint(20) NOT NULL COMMENT '消息主表ID',
  `user_id` bigint(20) NOT NULL COMMENT '接收人用户ID',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读 0-未读 1-已读（管理端可修改）',
  `read_time` datetime DEFAULT NULL COMMENT '首次点击阅读的时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '接收者自己是否删除了这条消息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_user` (`notification_id`, `user_id`), -- 防止重复插入
  KEY `idx_receiver_read` (`user_id`, `create_time`) -- 关键索引，用于查询未读列表
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知接收人状态表';

-- 3) 迁移存量已读状态到 receiver（在删除 is_read 之前；INSERT IGNORE 幂等）
INSERT IGNORE INTO `sys_notifications_receiver`
    (notification_id, user_id, is_read, read_time, is_deleted, create_time)
SELECT `id`, `user_id`, `is_read`, `create_time`, 0, `create_time`
FROM `sys_notifications`
WHERE `user_id` IS NOT NULL;

-- 4) 存量单接收人数据回填：把原有 user_id 写进 notice_push.user 数组
UPDATE `sys_notifications`
SET `notice_push` = JSON_OBJECT(
        'user', JSON_ARRAY(`user_id`),
        'dept', JSON_ARRAY(),
        'role', JSON_ARRAY()
    )
WHERE `notice_push` IS NULL AND `user_id` IS NOT NULL;

-- 5) 删除主表 is_read
ALTER TABLE `sys_notifications` DROP COLUMN `is_read`;

-- 6) 删除主表 user_id（接收人改由 notice_push 表达，不再是单接收人 user_id）
ALTER TABLE `sys_notifications` DROP COLUMN `user_id`;

-- 7) 公告表 sys_notice 增加推送对象 notice_push
--    管理端新增/编辑公告时选择发送对象（用户/部门/角色），null 或全空 = 全员可见
ALTER TABLE `sys_notice`
  ADD COLUMN `notice_push` json NULL COMMENT '推送对象 JSON：{"user":[],"dept":[],"role":[]}（null 或全空=全员可见）' AFTER `position`;
