-- 在线咨询（文字客服）
CREATE TABLE IF NOT EXISTS `chat_session` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `session_no`       VARCHAR(64)     NOT NULL,
  `access_token`     VARCHAR(64)     NOT NULL COMMENT '访客 WS/REST 凭证',
  `visitor_id`       VARCHAR(64)     NOT NULL COMMENT '浏览器本地 visitorId',
  `member_id`        BIGINT UNSIGNED NULL,
  `nickname`         VARCHAR(64)     NULL,
  `email`            VARCHAR(128)    NULL,
  `status`           VARCHAR(16)     NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN/CLOSED',
  `last_message_at`  DATETIME(3)     NULL,
  `last_preview`     VARCHAR(255)    NULL,
  `unread_admin`     INT             NOT NULL DEFAULT 0,
  `unread_visitor`   INT             NOT NULL DEFAULT 0,
  `created_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_session_no` (`session_no`),
  UNIQUE KEY `uk_chat_access_token` (`access_token`),
  KEY `idx_chat_visitor_status` (`visitor_id`, `status`),
  KEY `idx_chat_status_last` (`status`, `last_message_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='在线咨询会话';

CREATE TABLE IF NOT EXISTS `chat_message` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `session_id`    BIGINT UNSIGNED NOT NULL,
  `sender_type`   VARCHAR(16)     NOT NULL COMMENT 'VISITOR/ADMIN',
  `sender_id`     BIGINT UNSIGNED NULL,
  `content`       VARCHAR(1000)   NOT NULL,
  `created_at`    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_chat_msg_session` (`session_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='在线咨询消息';
