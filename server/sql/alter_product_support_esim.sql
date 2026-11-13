-- 交付方式独立开关：eSIM / 实体卡可多选
-- 旧逻辑：is_physical=0 → 用户可选 eSIM+实体；is_physical=1 → 仅实体
-- 新逻辑：support_esim + is_physical 各自控制

ALTER TABLE `product`
  ADD COLUMN `support_esim` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否支持 eSIM 交付' AFTER `is_physical`;

-- 旧「仅实体」：关闭 eSIM
UPDATE `product` SET `support_esim` = 0 WHERE `is_physical` = 1;

-- 旧「eSIM 类型(可双选)」：保持 eSIM，并打开实体支持
UPDATE `product` SET `is_physical` = 1 WHERE `is_physical` = 0;
