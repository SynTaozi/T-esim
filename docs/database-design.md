# T-esim 数据库设计说明

> 原则：**功能对齐 mocbase-sim，结构按成熟电商 / 多语言方案重做**，不以旧表结构为模板。

## 1. 设计目标

| 目标 | 做法 |
|------|------|
| 查询效率 | 覆盖国家关系表、SKU 可售单元、订单状态/会员/时间组合索引；避免宽表 JSON 扫库 |
| 规范分层 | 按域拆表：会员(UMS) / 商品(PMS) / 订单(OMS) / 支付(PAY) / 履约(FUL) / 营销(MKT) / 客服(CSS) / 内容(CMS) / 系统(SYS) |
| 中英文 | **Catalog / CMS 一律独立 `*_i18n` 表**，主表只保留业务字段；新增语言无需改表结构 |
| 订单历史 | 订单行冻结商品标题快照（`title_zh` / `title_en`），不依赖商品现价与现译 |

## 2. 多语言方案（成熟做法）

业界常见三种：同表多列（`name_en`）、JSON、**翻译表**。  
Sylius / Magento / 多数 SaaS 推荐 **翻译表**：

```
product ──1:N── product_i18n (locale, name, description, ...)
```

- `locale`：`zh` / `en`（BCP 47，可扩展）
- 查询：`LEFT JOIN` 目标语言 + 默认语言 `COALESCE` 回退
- 加语言 = 插行，不是 `ALTER TABLE`

**订单快照例外**：下单瞬间写入 `title_zh`/`title_en`（历史定格，不是直播译字段）。

## 3. 商品与订单（相对旧方案的核心变化）

### 旧方案问题（仅作对比）

- 商品表中英文字段混排，列膨胀
- 覆盖国家 / 地区塞 JSON，难按国家筛选
- 月份档位与订单行耦合松散，可售单元不清晰
- 实体卡 / 号卡 / 套餐多套目录表，下单逻辑分叉
- 订单头塞支付、物流、上游履约过多字段，职责不清

### 新方案

```
product_category (+ i18n)
       │
    product (+ i18n)          ← 业务属性、类型、上下架
       │
  product_sku                 ← 真正可售单元（月数档 / 设备规格）
       │
  product_coverage            ← 覆盖国家（规范化，可索引）
       │
  cart_item / sales_order_item
```

订单采用 Magento 风格命名，避免 SQL 保留字：

```
sales_order
  ├─ sales_order_item
  ├─ sales_order_address
  ├─ sales_order_payment
  └─ sales_order_shipment
```

## 4. 金额约定

- 货币：`USD`（`currency` 字段预留）
- 金额：`DECIMAL(12,2)`（业务可读；关键账务路径应用层保证精度）
- 佣金内部可用「分」存储的表单独标明（`*_cent`）

## 5. 功能覆盖对照（旧功能 → 新表）

| 旧能力 | 新表 |
|--------|------|
| 用户/余额/邀请 | `member` |
| 收货地址 | `member_address` |
| 验证码 | `auth_verification_code` |
| 分类/子分类 | `product_category`（`parent_id` 树）+ i18n |
| 套餐/实体卡/号卡目录 | 统一 `product` + `product_kind` |
| 月份定价 | `product_sku` |
| 覆盖国家 | `product_coverage` |
| 购物车 | `cart_item` |
| 订单/明细 | `sales_order` / `sales_order_item` |
| 支付方式 | `payment_channel` |
| 充值 | `wallet_recharge` |
| 用户已购号码/套餐 | `member_subscription` |
| Talk2All | `supplier_channel` / `supplier_sku_map` |
| 工单 | `ticket`（subject 为用户原文）/ `ticket_message` |
| 优惠券 | `coupon` / `coupon_usage` |
| 佣金 | `commission_*` |
| FAQ/教程/下载二维码 | `cms_*` + i18n |
| 站点/邮件/钉钉/主题 | `sys_*` |

## 6. 文件位置

- 建库脚本：`server/sql/01_schema.sql`
- 本说明：`docs/database-design.md`

后续可按模块拆 `02_seed_locale.sql`、`03_seed_payment_channel.sql` 等。
