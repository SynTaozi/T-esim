-- =============================================================================
-- T-esim 初始库表结构
-- 引擎/字符集：InnoDB + utf8mb4
-- 说明：详见 docs/database-design.md
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `t_esim`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `t_esim`;

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- SYS · 系统 / 语言
-- -----------------------------------------------------------------------------

CREATE TABLE `sys_locale` (
  `code`         VARCHAR(16)  NOT NULL COMMENT 'BCP47，如 zh / en',
  `name`         VARCHAR(64)  NOT NULL COMMENT '展示名',
  `is_default`   TINYINT(1)   NOT NULL DEFAULT 0,
  `sort_order`   INT          NOT NULL DEFAULT 0,
  `enabled`      TINYINT(1)   NOT NULL DEFAULT 1,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支持的语言';

INSERT INTO `sys_locale` (`code`, `name`, `is_default`, `sort_order`, `enabled`) VALUES
('zh', '中文', 1, 1, 1),
('en', 'English', 0, 2, 1);

-- -----------------------------------------------------------------------------
-- UMS · 会员
-- -----------------------------------------------------------------------------

CREATE TABLE `member` (
  `id`                         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username`                   VARCHAR(64)     NOT NULL,
  `email`                      VARCHAR(128)    NOT NULL,
  `password_hash`              VARCHAR(255)    NOT NULL,
  `full_name`                  VARCHAR(128)    NULL,
  `phone`                      VARCHAR(32)     NULL,
  `status`                     VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE/BANNED',
  `role`                       VARCHAR(16)     NOT NULL DEFAULT 'USER' COMMENT 'USER/ADMIN',
  `wallet_balance`             DECIMAL(12,2)   NOT NULL DEFAULT 0.00 COMMENT '余额 USD',
  `commission_balance`         DECIMAL(12,2)   NOT NULL DEFAULT 0.00 COMMENT '可提现佣金 USD',
  `commission_frozen`          DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
  `referral_code`              VARCHAR(32)     NULL,
  `referred_by`                BIGINT UNSIGNED NULL COMMENT '邀请人 member.id',
  `commission_rate_override`   DECIMAL(8,4)    NULL COMMENT '个人佣金比例覆盖，空则用全局',
  `settlement_days_override`   INT             NULL,
  `min_withdrawal_override`    DECIMAL(12,2)   NULL,
  `max_withdrawal_override`    DECIMAL(12,2)   NULL,
  `created_at`                 DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`                 DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted`                    TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_username` (`username`),
  UNIQUE KEY `uk_member_email` (`email`),
  UNIQUE KEY `uk_member_referral` (`referral_code`),
  KEY `idx_member_referred_by` (`referred_by`),
  KEY `idx_member_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员';

CREATE TABLE `member_address` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `member_id`       BIGINT UNSIGNED NOT NULL,
  `receiver_name`   VARCHAR(64)     NOT NULL,
  `phone`           VARCHAR(32)     NOT NULL,
  `email`           VARCHAR(128)    NULL,
  `country`         VARCHAR(64)     NULL,
  `province`        VARCHAR(64)     NULL,
  `city`            VARCHAR(64)     NULL,
  `address_line`    VARCHAR(255)    NOT NULL,
  `postal_code`     VARCHAR(32)     NULL,
  `is_default`      TINYINT(1)      NOT NULL DEFAULT 0,
  `created_at`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_address_member` (`member_id`),
  CONSTRAINT `fk_address_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员收货地址';

CREATE TABLE `auth_verification_code` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `email`       VARCHAR(128)    NOT NULL,
  `code`        VARCHAR(16)     NOT NULL,
  `scene`       VARCHAR(32)     NOT NULL COMMENT 'REGISTER/RESET_PASSWORD/LOGIN',
  `expires_at`  DATETIME(3)     NOT NULL,
  `used`        TINYINT(1)      NOT NULL DEFAULT 0,
  `created_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_vc_email_scene` (`email`, `scene`, `used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证码';

-- -----------------------------------------------------------------------------
-- PMS · 商品目录（主表无文案；文案在 *_i18n）
-- -----------------------------------------------------------------------------

CREATE TABLE `product_category` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `parent_id`     BIGINT UNSIGNED NULL COMMENT '空=一级分类',
  `code`          VARCHAR(64)     NOT NULL COMMENT '业务编码，如 PHONE / ESIM_DATA',
  `icon`          VARCHAR(255)    NULL,
  `sort_order`    INT             NOT NULL DEFAULT 0,
  `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `show_on_front` TINYINT(1)      NOT NULL DEFAULT 1,
  `created_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`code`),
  KEY `idx_category_parent_sort` (`parent_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类（树）';

CREATE TABLE `product_category_i18n` (
  `category_id`  BIGINT UNSIGNED NOT NULL,
  `locale`       VARCHAR(16)     NOT NULL,
  `name`         VARCHAR(128)    NOT NULL,
  `description`  VARCHAR(512)    NULL,
  PRIMARY KEY (`category_id`, `locale`),
  CONSTRAINT `fk_cat_i18n_cat` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类多语言';

CREATE TABLE `product` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `category_id`        BIGINT UNSIGNED NULL,
  `spu_code`           VARCHAR(64)     NOT NULL COMMENT 'SPU 编码',
  `product_kind`       VARCHAR(32)     NOT NULL COMMENT 'PLAN/DEVICE/ADDON',
  `plan_mode`          TINYINT         NULL COMMENT '套餐模式：0固定 1日租 2试用',
  `topup_mode`         TINYINT         NULL COMMENT '续费：0无 1支持续费 2续费+日租',
  `data_limit`         VARCHAR(64)     NULL COMMENT '流量规格展示值，如 10GB',
  `base_validity_days` INT             NULL COMMENT '基础有效天数（SKU 可覆盖）',
  `support_sms`        TINYINT(1)      NOT NULL DEFAULT 0,
  `support_data`       TINYINT(1)      NOT NULL DEFAULT 0,
  `support_voice`      TINYINT(1)      NOT NULL DEFAULT 0,
  `is_physical`        TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否实体发货',
  `cost_price`         DECIMAL(12,2)   NULL COMMENT '上游/卡成本',
  `physical_cost`      DECIMAL(12,2)   NULL COMMENT '实体成本',
  `image_url`          VARCHAR(512)    NULL,
  `display_image_url`  VARCHAR(512)    NULL,
  `status`             VARCHAR(16)     NOT NULL DEFAULT 'OFF_SALE' COMMENT 'DRAFT/ON_SALE/OFF_SALE',
  `is_featured`        TINYINT(1)      NOT NULL DEFAULT 0,
  `is_hot`             TINYINT(1)      NOT NULL DEFAULT 0,
  `sort_order`         INT             NOT NULL DEFAULT 0,
  `supplier_flag`      VARCHAR(32)     NULL COMMENT '如 TALK2ALL',
  `created_at`         DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`         DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted`            TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_spu` (`spu_code`),
  KEY `idx_product_cat_status_sort` (`category_id`, `status`, `sort_order`),
  KEY `idx_product_kind_status` (`product_kind`, `status`),
  KEY `idx_product_hot` (`is_hot`, `status`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品 SPU';

CREATE TABLE `product_i18n` (
  `product_id`   BIGINT UNSIGNED NOT NULL,
  `locale`       VARCHAR(16)     NOT NULL,
  `name`         VARCHAR(255)    NOT NULL,
  `summary`      VARCHAR(512)    NULL,
  `description`  TEXT            NULL,
  `features`     TEXT            NULL COMMENT '特性说明，多行文本',
  `notes`        TEXT            NULL COMMENT '重要说明/注意事项',
  PRIMARY KEY (`product_id`, `locale`),
  KEY `idx_product_i18n_name` (`locale`, `name`),
  CONSTRAINT `fk_product_i18n` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品多语言';

CREATE TABLE `product_sku` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `product_id`       BIGINT UNSIGNED NOT NULL,
  `sku_code`         VARCHAR(64)     NOT NULL,
  `months`           INT             NULL COMMENT '月付档位；设备类可空',
  `duration_days`    INT             NULL COMMENT '覆盖天数（可与 months 并存）',
  `sale_price`       DECIMAL(12,2)   NOT NULL COMMENT '售价 USD',
  `price_per_month`  DECIMAL(12,2)   NULL,
  `original_price`   DECIMAL(12,2)   NULL COMMENT '划线价',
  `stock`            INT             NULL COMMENT '空=不限库存',
  `is_default`       TINYINT(1)      NOT NULL DEFAULT 0,
  `status`           TINYINT         NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  `sort_order`       INT             NOT NULL DEFAULT 0,
  `created_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  UNIQUE KEY `uk_sku_product_months` (`product_id`, `months`),
  KEY `idx_sku_product_status` (`product_id`, `status`, `sort_order`),
  CONSTRAINT `fk_sku_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可售 SKU（月份档/规格）';

CREATE TABLE `product_sku_i18n` (
  `sku_id`   BIGINT UNSIGNED NOT NULL,
  `locale`   VARCHAR(16)     NOT NULL,
  `label`    VARCHAR(128)    NULL COMMENT '档位标签，如 Best Value',
  PRIMARY KEY (`sku_id`, `locale`),
  CONSTRAINT `fk_sku_i18n` FOREIGN KEY (`sku_id`) REFERENCES `product_sku` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SKU 多语言标签';

CREATE TABLE `product_coverage` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `product_id`    BIGINT UNSIGNED NOT NULL,
  `country_code`  CHAR(2)         NOT NULL COMMENT 'ISO 3166-1 alpha-2',
  `region_code`   VARCHAR(32)     NULL COMMENT 'Global/Asia/... 可选',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coverage` (`product_id`, `country_code`),
  KEY `idx_coverage_country` (`country_code`),
  CONSTRAINT `fk_coverage_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品覆盖国家（规范化，可按国筛选）';

CREATE TABLE `product_display_rule` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`         VARCHAR(64)     NOT NULL,
  `name`         VARCHAR(128)    NOT NULL,
  `is_active`    TINYINT(1)      NOT NULL DEFAULT 0,
  `rule_json`    JSON            NULL COMMENT '首页排序等规则',
  `created_at`   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_display_rule_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品展示规则';

-- -----------------------------------------------------------------------------
-- OMS · 购物车 / 订单
-- -----------------------------------------------------------------------------

CREATE TABLE `cart_item` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `member_id`   BIGINT UNSIGNED NOT NULL,
  `sku_id`      BIGINT UNSIGNED NOT NULL,
  `quantity`    INT             NOT NULL DEFAULT 1,
  `created_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_member_sku` (`member_id`, `sku_id`),
  KEY `idx_cart_sku` (`sku_id`),
  CONSTRAINT `fk_cart_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cart_sku` FOREIGN KEY (`sku_id`) REFERENCES `product_sku` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车';

CREATE TABLE `sales_order` (
  `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no`              VARCHAR(64)     NOT NULL,
  `member_id`             BIGINT UNSIGNED NOT NULL,
  `order_type`            VARCHAR(32)     NOT NULL DEFAULT 'PURCHASE' COMMENT 'PURCHASE/RENEWAL',
  `status`                VARCHAR(32)     NOT NULL DEFAULT 'PENDING_PAYMENT'
                          COMMENT 'PENDING_PAYMENT/PAID/FULFILLING/SHIPPED/COMPLETED/CANCELLED',
  `currency`              CHAR(3)         NOT NULL DEFAULT 'USD',
  `goods_amount`          DECIMAL(12,2)   NOT NULL DEFAULT 0.00 COMMENT '商品合计',
  `shipping_amount`       DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
  `discount_amount`       DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
  `payable_amount`        DECIMAL(12,2)   NOT NULL DEFAULT 0.00 COMMENT '应付',
  `paid_amount`           DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
  `coupon_id`             BIGINT UNSIGNED NULL,
  `coupon_code`           VARCHAR(64)     NULL,
  `locale`                VARCHAR(16)     NOT NULL DEFAULT 'zh' COMMENT '下单语言',
  `buyer_email`           VARCHAR(128)    NOT NULL,
  `buyer_remark`          VARCHAR(500)    NULL COMMENT '用户备注',
  `seller_remark`         VARCHAR(500)    NULL COMMENT '商家备注',
  `renewal_of_order_id`   BIGINT UNSIGNED NULL COMMENT '续费来源订单',
  `paid_at`               DATETIME(3)     NULL,
  `cancelled_at`          DATETIME(3)     NULL,
  `completed_at`          DATETIME(3)     NULL,
  `created_at`            DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`            DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted`               TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_member_created` (`member_id`, `created_at`),
  KEY `idx_order_status_created` (`status`, `created_at`),
  KEY `idx_order_email` (`buyer_email`),
  CONSTRAINT `fk_order_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售订单头';

CREATE TABLE `sales_order_item` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`           BIGINT UNSIGNED NOT NULL,
  `product_id`         BIGINT UNSIGNED NULL,
  `sku_id`             BIGINT UNSIGNED NULL,
  `product_kind`       VARCHAR(32)     NULL,
  `sku_code`           VARCHAR(64)     NULL,
  `title_zh`           VARCHAR(255)    NOT NULL COMMENT '下单快照·中文名',
  `title_en`           VARCHAR(255)    NULL COMMENT '下单快照·英文名',
  `months`             INT             NULL,
  `quantity`           INT             NOT NULL DEFAULT 1,
  `unit_price`         DECIMAL(12,2)   NOT NULL,
  `price_per_month`    DECIMAL(12,2)   NULL,
  `line_amount`        DECIMAL(12,2)   NOT NULL COMMENT '行小计',
  `created_at`         DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_oi_order` (`order_id`),
  KEY `idx_oi_sku` (`sku_id`),
  KEY `idx_oi_product` (`product_id`),
  CONSTRAINT `fk_oi_order` FOREIGN KEY (`order_id`) REFERENCES `sales_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售订单行（含标题快照）';

CREATE TABLE `sales_order_address` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`        BIGINT UNSIGNED NOT NULL,
  `address_type`    VARCHAR(16)     NOT NULL DEFAULT 'SHIPPING' COMMENT 'SHIPPING/BILLING',
  `receiver_name`   VARCHAR(64)     NOT NULL,
  `phone`           VARCHAR(32)     NOT NULL,
  `email`           VARCHAR(128)    NULL,
  `country`         VARCHAR(64)     NULL,
  `province`        VARCHAR(64)     NULL,
  `city`            VARCHAR(64)     NULL,
  `address_line`    VARCHAR(255)    NOT NULL,
  `postal_code`     VARCHAR(32)     NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_addr_type` (`order_id`, `address_type`),
  CONSTRAINT `fk_oa_order` FOREIGN KEY (`order_id`) REFERENCES `sales_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单地址快照';

CREATE TABLE `sales_order_payment` (
  `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`             BIGINT UNSIGNED NOT NULL,
  `channel_code`         VARCHAR(32)     NOT NULL COMMENT 'USDT/WECHAT/ALIPAY/BALANCE/...',
  `pay_status`           VARCHAR(32)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED/EXPIRED/REFUNDED',
  `amount`               DECIMAL(12,2)   NOT NULL,
  `currency`             CHAR(3)         NOT NULL DEFAULT 'USD',
  `third_party_trade_no` VARCHAR(128)    NULL,
  `transaction_id`       VARCHAR(128)    NULL,
  `crypto_amount`        DECIMAL(18,8)   NULL COMMENT '如 USDT 数量',
  `crypto_address`       VARCHAR(255)    NULL,
  `expire_at`            DATETIME(3)     NULL,
  `paid_at`              DATETIME(3)     NULL,
  `notify_payload`       JSON            NULL,
  `created_at`           DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`           DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_op_order` (`order_id`),
  KEY `idx_op_status_expire` (`pay_status`, `expire_at`),
  KEY `idx_op_third` (`third_party_trade_no`),
  CONSTRAINT `fk_op_order` FOREIGN KEY (`order_id`) REFERENCES `sales_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单支付单';

CREATE TABLE `sales_order_shipment` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`           BIGINT UNSIGNED NOT NULL,
  `ship_status`        VARCHAR(32)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SHIPPED/DELIVERED',
  `carrier_code`       VARCHAR(32)     NULL,
  `tracking_no`        VARCHAR(128)    NULL,
  `shipping_phone`     VARCHAR(32)     NULL,
  `shipping_remark`    VARCHAR(500)    NULL,
  `esim_qr_payload`    TEXT            NULL COMMENT 'eSIM 激活信息/二维码内容',
  `shipped_at`         DATETIME(3)     NULL,
  `email_sent`         TINYINT(1)      NOT NULL DEFAULT 0,
  `created_at`         DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`         DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_os_order` (`order_id`),
  KEY `idx_os_tracking` (`tracking_no`),
  CONSTRAINT `fk_os_order` FOREIGN KEY (`order_id`) REFERENCES `sales_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单发货/履约物流';

CREATE TABLE `sales_order_notice` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`     BIGINT UNSIGNED NOT NULL,
  `notice_type`  VARCHAR(32)     NOT NULL COMMENT 'CUSTOMER/STOCK/PAYMENT/...',
  `subject`      VARCHAR(255)    NULL,
  `content`      TEXT            NOT NULL,
  `sent_at`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_on_order` (`order_id`),
  CONSTRAINT `fk_on_order` FOREIGN KEY (`order_id`) REFERENCES `sales_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单客户通知记录';

-- -----------------------------------------------------------------------------
-- PAY · 支付渠道 / 钱包充值
-- -----------------------------------------------------------------------------

CREATE TABLE `payment_channel` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`        VARCHAR(32)     NOT NULL,
  `icon`        VARCHAR(255)    NULL,
  `enabled`     TINYINT(1)      NOT NULL DEFAULT 0,
  `sort_order`  INT             NOT NULL DEFAULT 0,
  `extra_json`  JSON            NULL,
  `created_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_channel_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付渠道';

CREATE TABLE `payment_channel_i18n` (
  `channel_id`   BIGINT UNSIGNED NOT NULL,
  `locale`       VARCHAR(16)     NOT NULL,
  `name`         VARCHAR(64)     NOT NULL,
  `description`  VARCHAR(255)    NULL,
  PRIMARY KEY (`channel_id`, `locale`),
  CONSTRAINT `fk_pc_i18n` FOREIGN KEY (`channel_id`) REFERENCES `payment_channel` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付渠道多语言';

CREATE TABLE `wallet_recharge` (
  `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `recharge_no`          VARCHAR(64)     NOT NULL,
  `member_id`            BIGINT UNSIGNED NOT NULL,
  `amount`               DECIMAL(12,2)   NOT NULL,
  `biz_type`             VARCHAR(32)     NOT NULL DEFAULT 'RECHARGE' COMMENT 'RECHARGE/RENEWAL',
  `related_order_id`     BIGINT UNSIGNED NULL,
  `channel_code`         VARCHAR(32)     NOT NULL,
  `status`               VARCHAR(32)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/FAILED/REFUNDED',
  `third_party_trade_no` VARCHAR(128)    NULL,
  `paid_at`              DATETIME(3)     NULL,
  `remark`               VARCHAR(255)    NULL,
  `created_at`           DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`           DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recharge_no` (`recharge_no`),
  KEY `idx_recharge_member` (`member_id`, `created_at`),
  KEY `idx_recharge_status` (`status`),
  CONSTRAINT `fk_recharge_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='余额充值单';

-- -----------------------------------------------------------------------------
-- FUL · 履约资产 / 上游供应商
-- -----------------------------------------------------------------------------

CREATE TABLE `member_subscription` (
  `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `member_id`         BIGINT UNSIGNED NOT NULL,
  `order_id`          BIGINT UNSIGNED NULL,
  `order_item_id`     BIGINT UNSIGNED NULL,
  `product_id`        BIGINT UNSIGNED NULL COMMENT '来源商品',
  `sku_id`            BIGINT UNSIGNED NULL,
  `msisdn`            VARCHAR(64)     NULL COMMENT '号码/标识',
  `country_code`      CHAR(2)         NULL,
  `carrier`           VARCHAR(64)     NULL,
  `plan_snapshot`     VARCHAR(255)    NULL,
  `purchase_price`    DECIMAL(12,2)   NULL,
  `renewal_price`     DECIMAL(12,2)   NULL,
  `status`            VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/EXPIRED/CANCELLED',
  `activated_at`      DATETIME(3)     NULL,
  `expires_at`        DATETIME(3)     NULL,
  `created_at`        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_sub_member_status` (`member_id`, `status`),
  KEY `idx_sub_expires` (`expires_at`),
  KEY `idx_sub_order` (`order_id`),
  CONSTRAINT `fk_sub_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户已购套餐/号码资产';

CREATE TABLE `supplier_channel` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`             VARCHAR(32)     NOT NULL COMMENT '如 TALK2ALL',
  `name`             VARCHAR(128)    NOT NULL,
  `environment`      VARCHAR(16)     NOT NULL DEFAULT 'PROD' COMMENT 'TEST/PROD',
  `base_url`         VARCHAR(255)    NULL,
  `credential_json`  JSON            NULL COMMENT '密钥等，注意加密存储',
  `callback_url`     VARCHAR(255)    NULL,
  `enabled`          TINYINT(1)      NOT NULL DEFAULT 0,
  `created_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code_env` (`code`, `environment`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上游供应商通道';

CREATE TABLE `supplier_sku_map` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `channel_id`       BIGINT UNSIGNED NOT NULL,
  `product_id`       BIGINT UNSIGNED NOT NULL,
  `sku_id`           BIGINT UNSIGNED NULL,
  `external_plan_id` VARCHAR(64)     NOT NULL,
  `external_name`    VARCHAR(255)    NULL,
  `auto_fulfill`     TINYINT(1)      NOT NULL DEFAULT 0,
  `extra_json`       JSON            NULL,
  `created_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_map` (`channel_id`, `product_id`, `sku_id`),
  KEY `idx_ssm_product` (`product_id`),
  CONSTRAINT `fk_ssm_channel` FOREIGN KEY (`channel_id`) REFERENCES `supplier_channel` (`id`),
  CONSTRAINT `fk_ssm_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地商品与上游套餐映射';

CREATE TABLE `fulfillment_task` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`        BIGINT UNSIGNED NOT NULL,
  `channel_id`      BIGINT UNSIGNED NULL,
  `task_status`     VARCHAR(32)     NOT NULL DEFAULT 'PENDING'
                    COMMENT 'PENDING/SUCCESS/FAILED/RETRYING',
  `retry_count`     INT             NOT NULL DEFAULT 0,
  `next_retry_at`   DATETIME(3)     NULL,
  `external_ref`    VARCHAR(128)    NULL,
  `error_message`   VARCHAR(1000)   NULL,
  `created_at`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_ft_order` (`order_id`),
  KEY `idx_ft_retry` (`task_status`, `next_retry_at`),
  CONSTRAINT `fk_ft_order` FOREIGN KEY (`order_id`) REFERENCES `sales_order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上游自动履约任务';

-- -----------------------------------------------------------------------------
-- MKT · 优惠券 / 佣金
-- -----------------------------------------------------------------------------

CREATE TABLE `coupon` (
  `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`              VARCHAR(64)     NOT NULL,
  `discount_amount`   DECIMAL(12,2)   NOT NULL COMMENT '固定减免 USD',
  `use_type`          VARCHAR(16)     NOT NULL DEFAULT 'ONCE' COMMENT 'ONCE/UNLIMITED',
  `category_id`       BIGINT UNSIGNED NULL COMMENT '限定分类，空=全场',
  `min_order_amount`  DECIMAL(12,2)   NULL,
  `status`            VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/EXHAUSTED/DISABLED',
  `starts_at`         DATETIME(3)     NULL,
  `expires_at`        DATETIME(3)     NULL,
  `remark`            VARCHAR(255)    NULL,
  `created_at`        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_code` (`code`),
  KEY `idx_coupon_status_exp` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券';

CREATE TABLE `coupon_usage` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `coupon_id`        BIGINT UNSIGNED NOT NULL,
  `coupon_code`      VARCHAR(64)     NOT NULL,
  `order_id`         BIGINT UNSIGNED NULL,
  `order_no`         VARCHAR(64)     NULL,
  `member_id`        BIGINT UNSIGNED NULL,
  `discount_amount`  DECIMAL(12,2)   NOT NULL,
  `status`           VARCHAR(16)     NOT NULL DEFAULT 'RESERVED' COMMENT 'RESERVED/USED/RELEASED',
  `used_at`          DATETIME(3)     NULL,
  `created_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_cu_coupon` (`coupon_id`),
  KEY `idx_cu_order` (`order_id`),
  KEY `idx_cu_member` (`member_id`),
  CONSTRAINT `fk_cu_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券占用/核销';

CREATE TABLE `commission_config` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `config_key`    VARCHAR(64)     NOT NULL,
  `config_value`  VARCHAR(512)    NOT NULL,
  `description`   VARCHAR(255)    NULL,
  `updated_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cc_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='佣金全局配置';

CREATE TABLE `commission_record` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `member_id`        BIGINT UNSIGNED NOT NULL COMMENT '受益人（邀请人）',
  `from_member_id`   BIGINT UNSIGNED NOT NULL COMMENT '贡献人',
  `order_id`         BIGINT UNSIGNED NULL,
  `order_no`         VARCHAR(64)     NULL,
  `recharge_no`      VARCHAR(64)     NULL,
  `order_amount`     DECIMAL(12,2)   NOT NULL,
  `commission_rate`  DECIMAL(8,4)   NOT NULL,
  `commission_amount` DECIMAL(12,2)  NOT NULL,
  `status`           VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SETTLED/CANCELLED',
  `settlement_date`  DATE            NULL,
  `settled_at`       DATETIME(3)     NULL,
  `created_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_cr_member_status` (`member_id`, `status`),
  KEY `idx_cr_settle` (`status`, `settlement_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='佣金流水';

CREATE TABLE `commission_withdrawal` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `withdrawal_no`    VARCHAR(64)     NOT NULL,
  `member_id`        BIGINT UNSIGNED NOT NULL,
  `amount`           DECIMAL(12,2)   NOT NULL,
  `withdraw_type`    VARCHAR(16)     NOT NULL COMMENT 'WITHDRAW/TO_BALANCE',
  `status`           VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/REJECTED',
  `pay_method`       VARCHAR(32)     NULL COMMENT 'alipay/wechat/bank',
  `pay_account_json` JSON            NULL,
  `admin_remark`     VARCHAR(255)    NULL,
  `admin_id`         BIGINT UNSIGNED NULL,
  `paid_at`          DATETIME(3)     NULL,
  `created_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cw_no` (`withdrawal_no`),
  KEY `idx_cw_member` (`member_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='佣金提现';

-- -----------------------------------------------------------------------------
-- CSS · 客服工单
-- -----------------------------------------------------------------------------

CREATE TABLE `ticket` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `ticket_no`    VARCHAR(64)     NOT NULL,
  `member_id`    BIGINT UNSIGNED NOT NULL,
  `order_id`     BIGINT UNSIGNED NULL,
  `category`     VARCHAR(32)     NOT NULL DEFAULT 'GENERAL' COMMENT 'GENERAL/REFUND/TECHNICAL/BILLING/OTHER',
  `subject`      VARCHAR(255)    NOT NULL COMMENT '用户提交原文，非目录翻译',
  `locale`       VARCHAR(16)     NOT NULL DEFAULT 'zh',
  `priority`     VARCHAR(16)     NOT NULL DEFAULT 'NORMAL',
  `status`       VARCHAR(16)     NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN/PENDING/RESOLVED/CLOSED',
  `created_at`   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket_no` (`ticket_no`),
  KEY `idx_ticket_member` (`member_id`, `status`),
  KEY `idx_ticket_status` (`status`, `updated_at`),
  CONSTRAINT `fk_ticket_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单';

CREATE TABLE `ticket_message` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `ticket_id`     BIGINT UNSIGNED NOT NULL,
  `sender_type`   VARCHAR(16)     NOT NULL COMMENT 'USER/ADMIN',
  `sender_id`     BIGINT UNSIGNED NULL,
  `sender_name`   VARCHAR(64)     NULL,
  `content`       TEXT            NOT NULL,
  `created_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_tm_ticket` (`ticket_id`, `created_at`),
  CONSTRAINT `fk_tm_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单消息';

-- -----------------------------------------------------------------------------
-- CMS · 内容
-- -----------------------------------------------------------------------------

CREATE TABLE `cms_faq_category` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `sort_order`  INT             NOT NULL DEFAULT 0,
  `status`      TINYINT         NOT NULL DEFAULT 1,
  `created_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ 分类';

CREATE TABLE `cms_faq_category_i18n` (
  `category_id`  BIGINT UNSIGNED NOT NULL,
  `locale`       VARCHAR(16)     NOT NULL,
  `name`         VARCHAR(128)    NOT NULL,
  PRIMARY KEY (`category_id`, `locale`),
  CONSTRAINT `fk_faq_cat_i18n` FOREIGN KEY (`category_id`) REFERENCES `cms_faq_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ 分类多语言';

CREATE TABLE `cms_faq` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `category_id`  BIGINT UNSIGNED NOT NULL,
  `sort_order`   INT             NOT NULL DEFAULT 0,
  `status`       TINYINT         NOT NULL DEFAULT 1,
  `created_at`   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_faq_cat` (`category_id`, `sort_order`),
  CONSTRAINT `fk_faq_cat` FOREIGN KEY (`category_id`) REFERENCES `cms_faq_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ';

CREATE TABLE `cms_faq_i18n` (
  `faq_id`    BIGINT UNSIGNED NOT NULL,
  `locale`    VARCHAR(16)     NOT NULL,
  `question`  VARCHAR(500)    NOT NULL,
  `answer`    TEXT            NOT NULL,
  PRIMARY KEY (`faq_id`, `locale`),
  CONSTRAINT `fk_faq_i18n` FOREIGN KEY (`faq_id`) REFERENCES `cms_faq` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ 多语言';

CREATE TABLE `cms_tutorial_category` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `icon`        VARCHAR(255)    NULL,
  `sort_order`  INT             NOT NULL DEFAULT 0,
  `status`      TINYINT         NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教程分类';

CREATE TABLE `cms_tutorial_category_i18n` (
  `category_id`  BIGINT UNSIGNED NOT NULL,
  `locale`       VARCHAR(16)     NOT NULL,
  `name`         VARCHAR(128)    NOT NULL,
  PRIMARY KEY (`category_id`, `locale`),
  CONSTRAINT `fk_tut_cat_i18n` FOREIGN KEY (`category_id`) REFERENCES `cms_tutorial_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教程分类多语言';

CREATE TABLE `cms_tutorial` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `category_id`   BIGINT UNSIGNED NOT NULL,
  `slug`          VARCHAR(128)    NOT NULL,
  `content_type`  VARCHAR(16)     NOT NULL DEFAULT 'ARTICLE' COMMENT 'ARTICLE/VIDEO/PDF',
  `cover_url`     VARCHAR(512)    NULL,
  `media_url`     VARCHAR(512)    NULL,
  `sort_order`    INT             NOT NULL DEFAULT 0,
  `status`        TINYINT         NOT NULL DEFAULT 1,
  `created_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tutorial_slug` (`slug`),
  KEY `idx_tutorial_cat` (`category_id`, `sort_order`),
  CONSTRAINT `fk_tutorial_cat` FOREIGN KEY (`category_id`) REFERENCES `cms_tutorial_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教程';

CREATE TABLE `cms_tutorial_i18n` (
  `tutorial_id`  BIGINT UNSIGNED NOT NULL,
  `locale`       VARCHAR(16)     NOT NULL,
  `title`        VARCHAR(255)    NOT NULL,
  `summary`      VARCHAR(512)    NULL,
  `content`      MEDIUMTEXT      NULL,
  PRIMARY KEY (`tutorial_id`, `locale`),
  CONSTRAINT `fk_tutorial_i18n` FOREIGN KEY (`tutorial_id`) REFERENCES `cms_tutorial` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教程多语言';

CREATE TABLE `cms_app_download` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `platform`      VARCHAR(32)     NOT NULL COMMENT 'IOS/ANDROID/...',
  `download_url`  VARCHAR(512)    NOT NULL,
  `qr_image_url`  VARCHAR(512)    NULL,
  `sort_order`    INT             NOT NULL DEFAULT 0,
  `status`        TINYINT         NOT NULL DEFAULT 1,
  `created_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='App 下载入口';

CREATE TABLE `cms_app_download_i18n` (
  `download_id`  BIGINT UNSIGNED NOT NULL,
  `locale`       VARCHAR(16)     NOT NULL,
  `name`         VARCHAR(128)    NOT NULL,
  `remark`       VARCHAR(255)    NULL,
  PRIMARY KEY (`download_id`, `locale`),
  CONSTRAINT `fk_app_dl_i18n` FOREIGN KEY (`download_id`) REFERENCES `cms_app_download` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='App 下载多语言';

CREATE TABLE `cms_site_setting` (
  `id`                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`                   VARCHAR(64)     NOT NULL DEFAULT 'default',
  `login_banner_url`       VARCHAR(512)    NULL,
  `register_banner_url`    VARCHAR(512)    NULL,
  `home_hero_image_url`    VARCHAR(512)    NULL,
  `default_shipping_fee`   DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
  `updated_at`             DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_site_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点展示配置';

-- -----------------------------------------------------------------------------
-- SYS · 邮件 / 钉钉 / 主题
-- -----------------------------------------------------------------------------

CREATE TABLE `sys_email_account` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`           VARCHAR(64)     NOT NULL,
  `email_address`  VARCHAR(128)    NOT NULL,
  `username`       VARCHAR(128)    NULL,
  `password`       VARCHAR(255)    NULL,
  `smtp_host`      VARCHAR(128)    NULL,
  `smtp_port`      INT             NULL,
  `imap_host`      VARCHAR(128)    NULL,
  `imap_port`      INT             NULL,
  `purpose`        VARCHAR(32)     NOT NULL COMMENT 'SHIPPING/PAYMENT/SUPPORT/GENERAL',
  `enabled`        TINYINT(1)      NOT NULL DEFAULT 0,
  `created_at`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_email_purpose` (`purpose`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发信账号';

CREATE TABLE `sys_email_template` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`         VARCHAR(64)     NOT NULL,
  `enabled`      TINYINT(1)      NOT NULL DEFAULT 1,
  `description`  VARCHAR(255)    NULL,
  `updated_at`   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_et_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件模板';

CREATE TABLE `sys_email_template_i18n` (
  `template_id`  BIGINT UNSIGNED NOT NULL,
  `locale`       VARCHAR(16)     NOT NULL,
  `name`         VARCHAR(128)    NOT NULL,
  `subject`      VARCHAR(255)    NOT NULL,
  `body`         MEDIUMTEXT      NOT NULL,
  PRIMARY KEY (`template_id`, `locale`),
  CONSTRAINT `fk_et_i18n` FOREIGN KEY (`template_id`) REFERENCES `sys_email_template` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件模板多语言';

CREATE TABLE `sys_dingtalk_config` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`          VARCHAR(64)     NOT NULL,
  `webhook_url`   VARCHAR(512)    NOT NULL,
  `enabled`       TINYINT(1)      NOT NULL DEFAULT 0,
  `description`   VARCHAR(255)    NULL,
  `created_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钉钉机器人';

CREATE TABLE `sys_dingtalk_notify_log` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `config_id`     BIGINT UNSIGNED NULL,
  `event_type`    VARCHAR(64)     NOT NULL COMMENT 'ORDER_PAID/NEW_TICKET/...',
  `title`         VARCHAR(255)    NULL,
  `content`       TEXT            NULL,
  `status`        VARCHAR(16)     NOT NULL,
  `error_message` VARCHAR(1000)   NULL,
  `created_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_ddl_type` (`event_type`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钉钉通知日志';

CREATE TABLE `sys_theme` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`        VARCHAR(64)     NOT NULL,
  `theme_key`   VARCHAR(64)     NOT NULL,
  `theme_name`  VARCHAR(128)    NOT NULL,
  `updated_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_theme_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统主题';
