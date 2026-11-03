-- 初始化前台可测支付方式：微信 / 支付宝 / 银行卡 / USDT（可重复执行）
INSERT INTO `payment_channel` (`code`, `icon`, `enabled`, `sort_order`)
SELECT 'WECHAT', 'wechat', 1, 10 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `payment_channel` WHERE `code` = 'WECHAT');

INSERT INTO `payment_channel` (`code`, `icon`, `enabled`, `sort_order`)
SELECT 'ALIPAY', 'alipay', 1, 20 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `payment_channel` WHERE `code` = 'ALIPAY');

INSERT INTO `payment_channel` (`code`, `icon`, `enabled`, `sort_order`)
SELECT 'BANK_CARD', 'bank_card', 1, 30 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `payment_channel` WHERE `code` = 'BANK_CARD');

INSERT INTO `payment_channel` (`code`, `icon`, `enabled`, `sort_order`)
SELECT 'USDT', 'usdt', 1, 40 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `payment_channel` WHERE `code` = 'USDT');

INSERT INTO `i18n_payment_channel` (`channel_id`, `locale`, `name`, `description`)
SELECT c.id, 'zh', '微信支付', NULL
FROM `payment_channel` c
WHERE c.code = 'WECHAT'
  AND NOT EXISTS (SELECT 1 FROM `i18n_payment_channel` i WHERE i.channel_id = c.id AND i.locale = 'zh');

INSERT INTO `i18n_payment_channel` (`channel_id`, `locale`, `name`, `description`)
SELECT c.id, 'en', 'WeChat Pay', NULL
FROM `payment_channel` c
WHERE c.code = 'WECHAT'
  AND NOT EXISTS (SELECT 1 FROM `i18n_payment_channel` i WHERE i.channel_id = c.id AND i.locale = 'en');

INSERT INTO `i18n_payment_channel` (`channel_id`, `locale`, `name`, `description`)
SELECT c.id, 'zh', '支付宝', NULL
FROM `payment_channel` c
WHERE c.code = 'ALIPAY'
  AND NOT EXISTS (SELECT 1 FROM `i18n_payment_channel` i WHERE i.channel_id = c.id AND i.locale = 'zh');

INSERT INTO `i18n_payment_channel` (`channel_id`, `locale`, `name`, `description`)
SELECT c.id, 'en', 'Alipay', NULL
FROM `payment_channel` c
WHERE c.code = 'ALIPAY'
  AND NOT EXISTS (SELECT 1 FROM `i18n_payment_channel` i WHERE i.channel_id = c.id AND i.locale = 'en');

INSERT INTO `i18n_payment_channel` (`channel_id`, `locale`, `name`, `description`)
SELECT c.id, 'zh', '银行卡', NULL
FROM `payment_channel` c
WHERE c.code = 'BANK_CARD'
  AND NOT EXISTS (SELECT 1 FROM `i18n_payment_channel` i WHERE i.channel_id = c.id AND i.locale = 'zh');

INSERT INTO `i18n_payment_channel` (`channel_id`, `locale`, `name`, `description`)
SELECT c.id, 'en', 'Bank Card', NULL
FROM `payment_channel` c
WHERE c.code = 'BANK_CARD'
  AND NOT EXISTS (SELECT 1 FROM `i18n_payment_channel` i WHERE i.channel_id = c.id AND i.locale = 'en');

INSERT INTO `i18n_payment_channel` (`channel_id`, `locale`, `name`, `description`)
SELECT c.id, 'zh', 'USDT', NULL
FROM `payment_channel` c
WHERE c.code = 'USDT'
  AND NOT EXISTS (SELECT 1 FROM `i18n_payment_channel` i WHERE i.channel_id = c.id AND i.locale = 'zh');

INSERT INTO `i18n_payment_channel` (`channel_id`, `locale`, `name`, `description`)
SELECT c.id, 'en', 'USDT', NULL
FROM `payment_channel` c
WHERE c.code = 'USDT'
  AND NOT EXISTS (SELECT 1 FROM `i18n_payment_channel` i WHERE i.channel_id = c.id AND i.locale = 'en');
