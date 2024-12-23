/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 50733
 Source Host           : localhost:3306
 Source Schema         : tiny-id

 Target Server Type    : MySQL
 Target Server Version : 50733
 File Encoding         : 65001

 Date: 27/04/2024 22:51:46
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for spring_session
-- ----------------------------
DROP TABLE IF EXISTS `spring_session`;
CREATE TABLE `spring_session`  (
  `PRIMARY_ID` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `SESSION_ID` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `CREATION_TIME` bigint(20) NOT NULL,
  `LAST_ACCESS_TIME` bigint(20) NOT NULL,
  `MAX_INACTIVE_INTERVAL` int(11) NOT NULL,
  `EXPIRY_TIME` bigint(20) NOT NULL,
  `PRINCIPAL_NAME` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`PRIMARY_ID`) USING BTREE,
  UNIQUE INDEX `SPRING_SESSION_IX1`(`SESSION_ID`) USING BTREE,
  INDEX `SPRING_SESSION_IX2`(`EXPIRY_TIME`) USING BTREE,
  INDEX `SPRING_SESSION_IX3`(`PRINCIPAL_NAME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for spring_session_attributes
-- ----------------------------
DROP TABLE IF EXISTS `spring_session_attributes`;
CREATE TABLE `spring_session_attributes`  (
  `SESSION_PRIMARY_ID` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `ATTRIBUTE_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `ATTRIBUTE_BYTES` blob NOT NULL,
  PRIMARY KEY (`SESSION_PRIMARY_ID`, `ATTRIBUTE_NAME`) USING BTREE,
  CONSTRAINT `SPRING_SESSION_ATTRIBUTES_FK` FOREIGN KEY (`SESSION_PRIMARY_ID`) REFERENCES `spring_session` (`PRIMARY_ID`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_idtable
-- ----------------------------
DROP TABLE IF EXISTS `t_idtable`;
CREATE TABLE `t_idtable`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键，内码',
  `id_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务流水号标识编码',
  `id_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务流水号名称',
  `id_value` bigint(20) NOT NULL DEFAULT 0 COMMENT '业务流水号当前值',
  `id_step` int(11) NOT NULL DEFAULT 100 COMMENT '步长',
  `id_length` int(11) NOT NULL COMMENT '业务流水号长度',
  `has_prefix` tinyint(4) NOT NULL COMMENT '是否有前缀 1有 0无',
  `id_prefix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务流水号前缀内容',
  `has_suffix` tinyint(4) NOT NULL COMMENT '是否有后缀 1有 0无',
  `id_suffix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务流水号后缀内容',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其他描述',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `created_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `t_idtable_idcode`(`id_code`) USING BTREE COMMENT '唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流水号表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_idtable
-- ----------------------------
INSERT INTO `t_idtable` VALUES (1, 'XXX', 'XXX', 3801, 100, 14, 1, NULL, 1, NULL, NULL, '2024-04-27 20:51:26', '2024-04-27 21:35:38', NULL, NULL);

-- ----------------------------
-- Table structure for t_worker_node
-- ----------------------------
DROP TABLE IF EXISTS `t_worker_node`;
CREATE TABLE `t_worker_node`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `server_ip` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务IP',
  `server_port` int(4) NOT NULL COMMENT '服务端口号',
  `worker_id` bigint(20) NOT NULL COMMENT '工作节点ID',
  `datacenter_id` bigint(20) NOT NULL COMMENT '数据中心ID',
  `total_id` bigint(20) NOT NULL COMMENT '总ID，由worker_id和datacenter_id计算得来',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `signed_at` datetime(0) NULL DEFAULT NULL COMMENT '记录时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `total_id_unique_index`(`total_id`) USING BTREE COMMENT '唯一索引',
  UNIQUE INDEX `worker_datacenter_unique_index`(`worker_id`, `datacenter_id`) USING BTREE COMMENT '联合唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '服务节点表' ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键，内码',
                           `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
                           `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户密码',
                           `nickname` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户昵称（中文姓名）',
                           `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
                           `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
                           `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态（0--正常 1--冻结）',
                           `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户头像',
                           `del_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '删除标志（0--未删除1--已删除）',
                           `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1, 'admin', '$2a$10$3g9ZFiwG.bM2XhQ1eDcqH.k0p0R10t9MU7HTQpJcT0.KoEIsb0z5G', '系统管理员', NULL, NULL, 0, NULL, 0, '2023-06-13 11:21:11', '2023-09-18 10:57:55');

SET FOREIGN_KEY_CHECKS = 1;
