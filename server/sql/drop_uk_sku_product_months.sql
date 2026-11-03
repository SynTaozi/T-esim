-- 允许同一产品下存在多条相同 months 的定价档
ALTER TABLE `product_sku` DROP INDEX `uk_sku_product_months`;
ALTER TABLE `product_sku` ADD INDEX `idx_sku_product_months` (`product_id`, `months`);
