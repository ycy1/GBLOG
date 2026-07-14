/*
 Navicat Premium Data Transfer

 Source Server         : 阿里-182.92.85.80
 Source Server Type    : MySQL
 Source Server Version : 80040
 Source Host           : 182.92.85.80:3306
 Source Schema         : shiyi_blog

 Target Server Type    : MySQL
 Target Server Version : 80040
 File Encoding         : 65001

 Date: 10/07/2026 14:13:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_agents
-- ----------------------------
DROP TABLE IF EXISTS `ai_agents`;
CREATE TABLE `ai_agents`  (
                              `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '智能体ID',
                              `config_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'default' COMMENT '配置类型 default.默认无 database.数据库 document.文档',
                              `config_id` bigint(0) NULL DEFAULT NULL COMMENT '配置id',
                              `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '智能体名称',
                              `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '智能体描述',
                              `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
                              `user_id` bigint(0) NOT NULL COMMENT '用户ID',
                              `system_prompt` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '系统提示词',
                              `greeting_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '欢迎语/开场白',
                              `model_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'gpt-4' COMMENT '模型类型: gpt-4, claude-3, qwen等',
                              `temperature` decimal(3, 2) NULL DEFAULT 0.70 COMMENT '温度参数',
                              `max_tokens` int(0) NULL DEFAULT 4096 COMMENT '最大输出token数',
                              `knowledge_base_ids` json NULL COMMENT '关联的知识库ID列表',
                              `retrieval_top_k` int(0) NULL DEFAULT 5 COMMENT '检索返回的文档数量',
                              `status` tinyint(0) NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
                              `is_public` tinyint(0) NULL DEFAULT 0 COMMENT '是否公开: 0-私有, 1-公开',
                              `created_by` bigint(0) NULL DEFAULT NULL COMMENT '创建者用户ID',
                              `total_conversations` int(0) NULL DEFAULT 0 COMMENT '总会话数',
                              `total_messages` int(0) NULL DEFAULT 0 COMMENT '总消息数',
                              `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                              `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
                              `deleted_at` datetime(0) NULL DEFAULT NULL COMMENT '软删除时间',
                              PRIMARY KEY (`id`) USING BTREE,
                              INDEX `idx_status`(`status`) USING BTREE,
                              INDEX `idx_created_by`(`created_by`) USING BTREE,
                              INDEX `idx_create_time`(`create_time`) USING BTREE,
                              INDEX `idx_deleted_at`(`deleted_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '智能体表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_config_source
-- ----------------------------
DROP TABLE IF EXISTS `ai_config_source`;
CREATE TABLE `ai_config_source`  (
                                     `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
                                     `source_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识源类型: database-数据库, document-文档',
                                     `source_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识源名称',
                                     `source_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '知识源描述',
                                     `db_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库类型: mysql, postgresql, oracle',
                                     `jdbc_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JDBC连接URL',
                                     `db_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库用户名',
                                     `db_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库密码（AES加密）',
                                     `doc_storage_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文档存储类型: local-本地, url-链接',
                                     `doc_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文档路径（本地路径或URL链接）',
                                     `chunk_size` int(0) NULL DEFAULT 500 COMMENT '文档分块大小（字符数）',
                                     `enabled` tinyint(0) NULL DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
                                     `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
                                     `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                     `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
                                     `updated_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                     `deleted` tinyint(0) NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE INDEX `uk_source_name`(`source_name`, `deleted`) USING BTREE,
                                     INDEX `idx_source_type`(`source_type`) USING BTREE,
                                     INDEX `idx_enabled`(`enabled`) USING BTREE,
                                     INDEX `idx_doc_storage_type`(`doc_storage_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识源配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_conversations
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversations`;
CREATE TABLE `ai_conversations`  (
                                     `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '会话ID',
                                     `agent_id` bigint(0) NOT NULL COMMENT '所属智能体ID',
                                     `user_id` bigint(0) NOT NULL COMMENT '用户ID',
                                     `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '新对话' COMMENT '会话标题',
                                     `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '会话摘要',
                                     `config` json NULL COMMENT '会话级配置: {temperature, max_tokens, system_prompt_override}',
                                     `context_window` int(0) NULL DEFAULT 10 COMMENT '上下文窗口大小（最近N轮）',
                                     `total_tokens_used` int(0) NULL DEFAULT 0 COMMENT '累计使用的token数',
                                     `message_count` int(0) NULL DEFAULT 0 COMMENT '消息总数',
                                     `status` tinyint(0) NULL DEFAULT 1 COMMENT '状态: 0-已结束, 1-活跃, 2-归档',
                                     `is_pinned` tinyint(0) NULL DEFAULT 0 COMMENT '是否置顶',
                                     `last_active_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '最后活跃时间',
                                     `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                     `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
                                     `deleted_at` datetime(0) NULL DEFAULT NULL COMMENT '软删除时间',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     INDEX `idx_agent_id`(`agent_id`) USING BTREE,
                                     INDEX `idx_user_id`(`user_id`) USING BTREE,
                                     INDEX `idx_status`(`status`) USING BTREE,
                                     INDEX `idx_last_active_at`(`last_active_at`) USING BTREE,
                                     INDEX `idx_create_time`(`create_time`) USING BTREE,
                                     INDEX `idx_deleted_at`(`deleted_at`) USING BTREE,
                                     CONSTRAINT `ai_conversations_ibfk_1` FOREIGN KEY (`agent_id`) REFERENCES `ai_agents` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_memories
-- ----------------------------
DROP TABLE IF EXISTS `ai_memories`;
CREATE TABLE `ai_memories`  (
                                `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '记忆ID',
                                `conversation_id` bigint(0) NOT NULL COMMENT '所属会话ID',
                                `agent_id` bigint(0) NOT NULL COMMENT '所属智能体ID（冗余字段，便于查询）',
                                `user_id` bigint(0) NOT NULL COMMENT '用户ID（冗余字段，便于查询）',
                                `role` enum('user','assistant','system','function') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色',
                                `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
                                `message_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'text' COMMENT '消息类型: text, image, file, code等',
                                `attachments` json NULL COMMENT '附件信息: [{type, url, name}]',
                                `function_call` json NULL COMMENT '函数调用信息',
                                `function_response` json NULL COMMENT '函数返回结果',
                                `parent_message_id` bigint(0) NULL DEFAULT NULL COMMENT '父消息ID（用于构建对话树）',
                                `turn_number` int(0) NOT NULL COMMENT '对话轮次编号（从1开始）',
                                `tokens_used` int(0) NULL DEFAULT 0 COMMENT '该消息使用的token数',
                                `tokens_cumulative` int(0) NULL DEFAULT 0 COMMENT '累计token数（该会话中）',
                                `importance_score` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '重要性评分（用于记忆压缩/遗忘）',
                                `is_summarized` tinyint(0) NULL DEFAULT 0 COMMENT '是否已被摘要压缩',
                                `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '摘要压缩内容',
                                `keywords` json NULL COMMENT '提取的关键词列表',
                                `entities` json NULL COMMENT '实体识别结果: [{type, value}]',
                                `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
                                `deleted_at` datetime(0) NULL DEFAULT NULL COMMENT '软删除时间',
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
                                INDEX `idx_agent_id`(`agent_id`) USING BTREE,
                                INDEX `idx_user_id`(`user_id`) USING BTREE,
                                INDEX `idx_role`(`role`) USING BTREE,
                                INDEX `idx_parent_message_id`(`parent_message_id`) USING BTREE,
                                INDEX `idx_turn_number`(`turn_number`) USING BTREE,
                                INDEX `idx_create_time`(`create_time`) USING BTREE,
                                INDEX `idx_is_summarized`(`is_summarized`) USING BTREE,
                                INDEX `idx_deleted_at`(`deleted_at`) USING BTREE,
                                CONSTRAINT `ai_memories_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversations` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '记忆表（消息记录表）' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
