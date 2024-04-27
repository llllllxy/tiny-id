

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
  `id_value` bigint(20) NOT NULL DEFAULT 1 COMMENT '业务流水号当前值',
  `id_length` int(11) NOT NULL COMMENT '业务流水号长度',
  `has_prefix` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否有前缀 0有，1没有',
  `id_prefix` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务流水号前缀内容',
  `has_suffix` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否有后缀 0有，1没有',
  `id_suffix` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务流水号后缀内容',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其他描述',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `created_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `t_idtable_idcode`(`id_code`) USING BTREE COMMENT '唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '流水号表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_worker_node
-- ----------------------------
DROP TABLE IF EXISTS `t_worker_node`;
CREATE TABLE `t_worker_node`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `server_ip` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务IP',
  `server_port` int(4) NOT NULL COMMENT '服务端口号',
  `worker_id` bigint(20) NOT NULL COMMENT '内容',
  `datacenter_id` bigint(20) NOT NULL COMMENT '邮箱',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `signed_at` datetime(0) NULL DEFAULT NULL COMMENT '记录时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `worker_datacenter_unique_index`(`worker_id`, `datacenter_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2024079000000003968 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '反馈建议表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_worker_node
-- ----------------------------
INSERT INTO `t_worker_node` VALUES (2024079000000003961, '192.168.0.107', 9999, 21, 2, '2024-04-27 13:51:38', '2024-04-27 13:51:38', '2024-04-27 13:51:39');
INSERT INTO `t_worker_node` VALUES (2024079000000003962, '192.168.0.107', 9999, 20, 2, '2024-04-27 13:56:34', '2024-04-27 13:56:34', '2024-04-27 13:56:34');
INSERT INTO `t_worker_node` VALUES (2024079000000003963, '192.168.0.107', 9999, 5, 2, '2024-04-27 14:09:58', '2024-04-27 14:09:58', '2024-04-27 14:09:59');
INSERT INTO `t_worker_node` VALUES (2024079000000003964, '192.168.0.107', 9999, 16, 2, '2024-04-27 14:13:51', '2024-04-27 14:13:51', '2024-04-27 14:13:51');
INSERT INTO `t_worker_node` VALUES (2024079000000003965, '192.168.0.107', 9999, 0, 2, '2024-04-27 15:33:33', '2024-04-27 16:36:33', '2024-04-27 16:36:34');
INSERT INTO `t_worker_node` VALUES (2024079000000003966, '192.168.0.107', 9999, 17, 2, '2024-04-27 16:36:58', '2024-04-27 16:39:58', '2024-04-27 16:39:59');
INSERT INTO `t_worker_node` VALUES (2024079000000003967, '192.168.0.107', 9999, 30, 2, '2024-04-27 16:40:34', '2024-04-27 20:11:48', '2024-04-27 20:11:49');

SET FOREIGN_KEY_CHECKS = 1;
