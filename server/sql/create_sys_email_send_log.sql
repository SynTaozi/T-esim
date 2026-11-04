-- 邮件发送记录（成功/失败）
CREATE TABLE IF NOT EXISTS `sys_email_send_log` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `account_id`     BIGINT UNSIGNED NULL COMMENT '发信账号',
  `purpose`        VARCHAR(32)     NULL COMMENT 'SHIPPING/PAYMENT/SUPPORT/GENERAL',
  `biz_type`       VARCHAR(64)     NOT NULL COMMENT 'AUTH_REGISTER_CODE/ORDER_SHIPPING/...',
  `biz_id`         VARCHAR(64)     NULL COMMENT '关联业务 ID，如订单号/工单号',
  `to_email`       VARCHAR(255)    NOT NULL,
  `subject`        VARCHAR(255)    NULL,
  `body`           MEDIUMTEXT      NULL,
  `status`         VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
  `error_message`  VARCHAR(1000)   NULL,
  `attempt_count`  INT             NOT NULL DEFAULT 0,
  `sent_at`        DATETIME(3)     NULL,
  `created_at`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_esl_status_created` (`status`, `created_at`),
  KEY `idx_esl_biz` (`biz_type`, `biz_id`),
  KEY `idx_esl_to` (`to_email`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件发送日志';
