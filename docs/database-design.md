# T-esim 数据库设计说明

> 原则：**功能对齐 mocbase-sim，结构按成熟电商方案重做**；多语言**仅中英**，文案独立表存放；**不使用数据库外键**，关联与级联删除由应用代码保证。

## 1. 多语言方案（已定稿）

**只支持 `zh` / `en`**，采用独立翻译表，**不用**主表 `*_zh` / `*_en` 双列（避免主表字段膨胀）。

| 约定 | 说明 |
|------|------|
| 命名 | 统一 **`i18n_` 前缀**，库内按名字排在一起，一眼能认出全部文案表 |
| 主键 | `(业务主键, locale)`，如 `(product_id, locale)` |
| 取值 | `locale` = `zh` / `en`（与 `sys_locale` 对齐） |
| 查询 | `LEFT JOIN i18n_* ON ... AND locale = ?`，缺译可回退默认语言 |
| UI 文案 | 按钮/菜单等仍用前端 `vue-i18n`，不进库 |

文案表一览：

| 表名 | 对应主表 |
|------|----------|
| `i18n_product_category` | `product_category` |
| `i18n_product` | `product` |
| `i18n_product_sku` | `product_sku` |
| `i18n_payment_channel` | `payment_channel` |
| `i18n_cms_faq_category` | `cms_faq_category` |
| `i18n_cms_faq` | `cms_faq` |
| `i18n_cms_tutorial_category` | `cms_tutorial_category` |
| `i18n_cms_tutorial` | `cms_tutorial` |
| `i18n_cms_app_download` | `cms_app_download` |
| `i18n_sys_email_template` | `sys_email_template` |

**例外**：订单行 `title_zh` / `title_en` 是**下单快照**（历史定格），不是目录翻译，不进 `i18n_*`。

## 2. 设计目标

| 目标 | 做法 |
|------|------|
| 主表精简 | 业务属性留主表；可译文案一律进 `i18n_*` |
| 查询效率 | 覆盖国家关系表、SKU 可售单元、订单组合索引 |
| 规范分层 | 会员 / 商品 / 订单 / 支付 / 履约 / 营销 / 客服 / 内容 / 系统 |
| 商品订单 | SPU + SKU；订单头与支付/物流拆表 |

## 3. 商品与订单

```
product_category ── i18n_product_category
       │
    product ── i18n_product
       │
  product_sku ── i18n_product_sku
       │
  product_coverage
       │
  cart_item / sales_order_item
```

订单：

```
sales_order
  ├─ sales_order_item      （含 title_zh / title_en / validity_days 快照）
  ├─ sales_order_address
  ├─ sales_order_payment
  ├─ sales_order_shipment  （实体物流）
  └─ sales_order_esim      （数字履约：二维码 / ICCID）
```

## 4. 金额与约束约定

- **无外键**：表间 `*_id` 仅保留索引；存在性、级联删除、孤儿数据清理均在 Service 层处理
- 货币：`USD`（`currency` 预留）
- 金额：`DECIMAL(12,2)`
- 钱包：`member.wallet_balance` + 只追加流水 `wallet_ledger`；改余额用 `member.version` 乐观锁
- 软删唯一：`member` / `product` 用生成列 `*_active`，软删后可复用用户名/邮箱/SPU
- SKU 月档唯一：`UNIQUE (product_id, months)`；套餐必须填 `months`，设备类置 `NULL`（多规格互不冲突）

## 5. 功能覆盖

| 能力 | 表 |
|------|-----|
| 语言字典 | `sys_locale`（seed：zh / en） |
| 用户/余额/邀请 | `member` + `wallet_ledger` |
| 地址 | `member_address` |
| 分类/商品/SKU/覆盖国 | `product_*` + `i18n_product*` |
| 购物车/订单 | `cart_item` / `sales_order*` |
| 实体物流 / eSIM | `sales_order_shipment` / `sales_order_esim` |
| 支付/充值 | `payment_channel` + `i18n_payment_channel` / `wallet_recharge` |
| 已购资产/上游 | `member_subscription` / `supplier_*` / `fulfillment_task` |
| 券/佣金 | `coupon*` / `commission_*` |
| 工单 | `ticket` / `ticket_message` |
| FAQ/教程等 | `cms_*` + `i18n_cms_*` |
| 邮件/钉钉 | `sys_*` + `i18n_sys_email_template` |

## 6. 文件

- `server/sql/01_schema.sql`
- 本文档
