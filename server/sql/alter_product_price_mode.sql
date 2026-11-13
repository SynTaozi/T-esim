-- 定价模式：MONTHLY=月份档定价，FLAT=一口价
-- 实体卡单独售价：physical_sale_price；为空则回退 sale_price（eSIM 价）

ALTER TABLE `product`
  ADD COLUMN `price_mode` VARCHAR(16) NOT NULL DEFAULT 'MONTHLY' COMMENT 'MONTHLY|FLAT' AFTER `support_esim`;

ALTER TABLE `product_sku`
  ADD COLUMN `physical_sale_price` DECIMAL(12,2) NULL COMMENT '实体卡售价；空则用 sale_price' AFTER `sale_price`;

-- 无月份档的商品默认改为一口价
UPDATE `product` p
SET p.`price_mode` = 'FLAT'
WHERE NOT EXISTS (
  SELECT 1 FROM `product_sku` s
  WHERE s.`product_id` = p.`id` AND s.`months` IS NOT NULL AND s.`months` > 0
);
