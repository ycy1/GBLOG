-- ============================================
-- 再次发送（send_code 发送批次）改造
-- 1) sys_notifications 主表：新增 send_code（32位 uuid，保证唯一）
ALTER TABLE `sys_notifications`
  ADD COLUMN `send_code` varchar(32) NULL COMMENT '发送批次标识（32位uuid，重新发送时生成新值）' AFTER `notice_push`,
  ADD UNIQUE KEY `uk_send_code` (`send_code`);

-- 2) sys_notifications_receiver 子表：新增 send_code，标记该接收记录所属发送批次
ALTER TABLE `sys_notifications_receiver`
  ADD COLUMN `send_code` varchar(32) NULL COMMENT '发送批次标识（对应主表 send_code）' AFTER `notification_id`;

-- 3) 存量主表数据回填：为已有消息生成 send_code（id 唯一，MD5 结果即 32 位十六进制，保证不冲突）
UPDATE `sys_notifications`
SET `send_code` = MD5(CONCAT('send-', id))
WHERE `send_code` IS NULL;
