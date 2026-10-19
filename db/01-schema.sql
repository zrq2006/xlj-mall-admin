-- =============================================================
-- 小蓝鲸优选 · 管理后台  表结构（使用项目用户 xlj_mall 执行）
-- mysql -u xlj_mall -pXljMall_2026 -h 127.0.0.1 xlj_mall < 01-schema.sql
-- =============================================================

USE `xlj_mall`;

-- 管理员账号 ------------------------------------------------
CREATE TABLE IF NOT EXISTS `admin_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
  `password`    VARCHAR(200) NOT NULL COMMENT 'PBKDF2 密码摘要',
  `nickname`    VARCHAR(50)  NOT NULL COMMENT '显示名称',
  `role`        VARCHAR(20)  NOT NULL DEFAULT 'operator' COMMENT '角色：admin=系统管理员, operator=运营人员',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_user_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台管理员';

-- 商品分类 --------------------------------------------------
CREATE TABLE IF NOT EXISTS `category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称',
  `icon`        VARCHAR(200) NULL COMMENT '分类图标（emoji 或图片地址）',
  `image`       VARCHAR(500) NULL COMMENT '分类图片',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=显示 0=隐藏',
  `created_by`  VARCHAR(50)  NOT NULL DEFAULT 'system' COMMENT '创建人',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_sort` (`sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类';

-- 品牌 ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `brand` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`          VARCHAR(100) NOT NULL COMMENT '品牌名称',
  `logo`          VARCHAR(500) NULL COMMENT '品牌 Logo（图片地址或 emoji）',
  `first_letter`  CHAR(1)      NOT NULL DEFAULT '' COMMENT '首字母',
  `description`   VARCHAR(500) NULL COMMENT '品牌描述',
  `sort`          INT          NOT NULL DEFAULT 0 COMMENT '排序值',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '1=显示 0=隐藏',
  `created_by`    VARCHAR(50)  NOT NULL DEFAULT 'system' COMMENT '创建人',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_brand_name` (`name`),
  KEY `idx_brand_sort` (`sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品品牌';

-- 商品 ------------------------------------------------------
CREATE TABLE IF NOT EXISTS `product` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`          VARCHAR(200)  NOT NULL COMMENT '商品名称',
  `description`   VARCHAR(1000) NULL COMMENT '商品描述 / 卖点',
  `category_id`   BIGINT        NULL COMMENT '分类 ID',
  `brand_id`      BIGINT        NULL COMMENT '品牌 ID',
  `price`         DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '售价',
  `market_price`  DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '同行价',
  `stock`         INT           NOT NULL DEFAULT 0 COMMENT '库存',
  `sales`         INT           NOT NULL DEFAULT 0 COMMENT '销量',
  `image`         VARCHAR(500)  NULL COMMENT '商品主图',
  `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '1=上架 0=下架',
  `created_by`    VARCHAR(50)   NOT NULL DEFAULT 'system' COMMENT '创建人',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_name` (`name`),
  KEY `idx_product_category` (`category_id`),
  KEY `idx_product_status` (`status`),
  KEY `idx_product_created` (`created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品';

-- 会员（工作台统计用） --------------------------------------
CREATE TABLE IF NOT EXISTS `member` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `nickname`    VARCHAR(50)  NOT NULL COMMENT '昵称',
  `phone`       VARCHAR(20)  NULL COMMENT '手机号',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '前台会员';

-- 入驻商家（工作台统计用） ----------------------------------
CREATE TABLE IF NOT EXISTS `merchant` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`          VARCHAR(100) NOT NULL COMMENT '商家名称',
  `audit_status`  TINYINT      NOT NULL DEFAULT 0 COMMENT '0=待审核 1=已通过 2=已驳回',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '入驻商家';

-- 订单（工作台统计用） --------------------------------------
CREATE TABLE IF NOT EXISTS `mall_order` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no`      VARCHAR(40)   NOT NULL COMMENT '订单号',
  `member_id`     BIGINT        NULL COMMENT '会员 ID',
  `total_amount`  DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额',
  `pay_amount`    DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  `status`        VARCHAR(20)   NOT NULL DEFAULT 'PENDING_PAY' COMMENT 'PENDING_PAY/PENDING_SHIP/SHIPPED/COMPLETED/CANCELLED',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `paid_at`       DATETIME      NULL COMMENT '支付时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_status` (`status`),
  KEY `idx_order_created` (`created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商城订单';
