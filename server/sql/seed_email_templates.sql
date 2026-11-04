-- 常用邮件模板（可在后台继续编辑）
INSERT INTO `sys_email_template` (`code`, `enabled`, `description`)
VALUES
  ('AUTH_REGISTER_CODE', 1, '注册验证码'),
  ('AUTH_RESET_CODE', 1, '重置密码验证码'),
  ('ORDER_SHIPPING', 1, '订单发货通知'),
  ('ORDER_CUSTOMER_NOTICE', 1, '订单客服通知'),
  ('TICKET_REPLY', 1, '工单回复通知')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

INSERT INTO `i18n_sys_email_template` (`template_id`, `locale`, `name`, `subject`, `body`)
SELECT t.id, 'zh', '注册验证码', '【Tesim】注册验证码',
       '<p>您好，</p><p>您的注册验证码是：<strong>{{code}}</strong></p><p>有效期 {{expireMinutes}} 分钟，请勿泄露给他人。</p>'
FROM `sys_email_template` t WHERE t.code = 'AUTH_REGISTER_CODE'
ON DUPLICATE KEY UPDATE `subject` = VALUES(`subject`), `body` = VALUES(`body`), `name` = VALUES(`name`);

INSERT INTO `i18n_sys_email_template` (`template_id`, `locale`, `name`, `subject`, `body`)
SELECT t.id, 'en', 'Register code', '[Tesim] Registration code',
       '<p>Hello,</p><p>Your registration code is: <strong>{{code}}</strong></p><p>It expires in {{expireMinutes}} minutes. Do not share it.</p>'
FROM `sys_email_template` t WHERE t.code = 'AUTH_REGISTER_CODE'
ON DUPLICATE KEY UPDATE `subject` = VALUES(`subject`), `body` = VALUES(`body`), `name` = VALUES(`name`);

INSERT INTO `i18n_sys_email_template` (`template_id`, `locale`, `name`, `subject`, `body`)
SELECT t.id, 'zh', '重置密码验证码', '【Tesim】重置密码验证码',
       '<p>您好，</p><p>您的重置密码验证码是：<strong>{{code}}</strong></p><p>有效期 {{expireMinutes}} 分钟。</p>'
FROM `sys_email_template` t WHERE t.code = 'AUTH_RESET_CODE'
ON DUPLICATE KEY UPDATE `subject` = VALUES(`subject`), `body` = VALUES(`body`), `name` = VALUES(`name`);

INSERT INTO `i18n_sys_email_template` (`template_id`, `locale`, `name`, `subject`, `body`)
SELECT t.id, 'en', 'Reset password code', '[Tesim] Password reset code',
       '<p>Hello,</p><p>Your password reset code is: <strong>{{code}}</strong></p><p>It expires in {{expireMinutes}} minutes.</p>'
FROM `sys_email_template` t WHERE t.code = 'AUTH_RESET_CODE'
ON DUPLICATE KEY UPDATE `subject` = VALUES(`subject`), `body` = VALUES(`body`), `name` = VALUES(`name`);

INSERT INTO `i18n_sys_email_template` (`template_id`, `locale`, `name`, `subject`, `body`)
SELECT t.id, 'zh', '发货通知', '【Tesim】订单 {{orderNo}} 已发货',
       '<p>您好，</p><p>您的订单 <strong>{{orderNo}}</strong> 已发货。</p><p>物流公司：{{carrier}}</p><p>物流单号：{{trackingNo}}</p><p>激活/二维码信息：{{activationCode}}</p><p>如有疑问请联系客服。</p>'
FROM `sys_email_template` t WHERE t.code = 'ORDER_SHIPPING'
ON DUPLICATE KEY UPDATE `subject` = VALUES(`subject`), `body` = VALUES(`body`), `name` = VALUES(`name`);

INSERT INTO `i18n_sys_email_template` (`template_id`, `locale`, `name`, `subject`, `body`)
SELECT t.id, 'en', 'Shipping notice', '[Tesim] Order {{orderNo}} shipped',
       '<p>Hello,</p><p>Your order <strong>{{orderNo}}</strong> has been shipped.</p><p>Carrier: {{carrier}}</p><p>Tracking No.: {{trackingNo}}</p><p>Activation / QR: {{activationCode}}</p>'
FROM `sys_email_template` t WHERE t.code = 'ORDER_SHIPPING'
ON DUPLICATE KEY UPDATE `subject` = VALUES(`subject`), `body` = VALUES(`body`), `name` = VALUES(`name`);

INSERT INTO `i18n_sys_email_template` (`template_id`, `locale`, `name`, `subject`, `body`)
SELECT t.id, 'zh', '订单通知', '{{subject}}',
       '<p>您好，</p><p>关于订单 <strong>{{orderNo}}</strong>：</p><div>{{content}}</div>'
FROM `sys_email_template` t WHERE t.code = 'ORDER_CUSTOMER_NOTICE'
ON DUPLICATE KEY UPDATE `subject` = VALUES(`subject`), `body` = VALUES(`body`), `name` = VALUES(`name`);

INSERT INTO `i18n_sys_email_template` (`template_id`, `locale`, `name`, `subject`, `body`)
SELECT t.id, 'en', 'Order notice', '{{subject}}',
       '<p>Hello,</p><p>Regarding order <strong>{{orderNo}}</strong>:</p><div>{{content}}</div>'
FROM `sys_email_template` t WHERE t.code = 'ORDER_CUSTOMER_NOTICE'
ON DUPLICATE KEY UPDATE `subject` = VALUES(`subject`), `body` = VALUES(`body`), `name` = VALUES(`name`);

INSERT INTO `i18n_sys_email_template` (`template_id`, `locale`, `name`, `subject`, `body`)
SELECT t.id, 'zh', '工单回复', '【Tesim】工单 {{ticketNo}} 有新回复',
       '<p>您好，</p><p>您的工单 <strong>{{ticketNo}}</strong>（{{ticketSubject}}）有新的客服回复：</p><blockquote>{{reply}}</blockquote><p>请登录网站查看详情。</p>'
FROM `sys_email_template` t WHERE t.code = 'TICKET_REPLY'
ON DUPLICATE KEY UPDATE `subject` = VALUES(`subject`), `body` = VALUES(`body`), `name` = VALUES(`name`);

INSERT INTO `i18n_sys_email_template` (`template_id`, `locale`, `name`, `subject`, `body`)
SELECT t.id, 'en', 'Ticket reply', '[Tesim] Reply on ticket {{ticketNo}}',
       '<p>Hello,</p><p>Your ticket <strong>{{ticketNo}}</strong> ({{ticketSubject}}) has a new reply:</p><blockquote>{{reply}}</blockquote><p>Please sign in to view details.</p>'
FROM `sys_email_template` t WHERE t.code = 'TICKET_REPLY'
ON DUPLICATE KEY UPDATE `subject` = VALUES(`subject`), `body` = VALUES(`body`), `name` = VALUES(`name`);
