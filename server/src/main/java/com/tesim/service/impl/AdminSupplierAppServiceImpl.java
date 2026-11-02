package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AdminSupplierAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesim.common.BizException;
import com.tesim.dto.admin.AdminSupplierConfigRequest;
import com.tesim.dto.admin.AdminSupplierConfigVO;
import com.tesim.dto.admin.AdminSupplierMappingRequest;
import com.tesim.dto.admin.AdminSupplierMappingVO;
import com.tesim.entity.Product;
import com.tesim.entity.ProductSku;
import com.tesim.entity.SupplierChannel;
import com.tesim.entity.SupplierSkuMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminSupplierAppServiceImpl implements AdminSupplierAppService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SupplierChannelService channelService;
    private final SupplierSkuMapService skuMapService;
    private final ProductService productService;
    private final ProductSkuService productSkuService;

    public List<AdminSupplierConfigVO> listConfigs() {
        return channelService.list(new LambdaQueryWrapper<SupplierChannel>()
                        .orderByDesc(SupplierChannel::getId))
                .stream().map(this::toConfigVO).toList();
    }

    public AdminSupplierConfigVO getConfig(Long id) {
        return toConfigVO(requireChannel(id));
    }

    @Transactional
    public AdminSupplierConfigVO createConfig(AdminSupplierConfigRequest req) {
        if (!StringUtils.hasText(req.getName())) {
            throw new BizException("配置名称不能为空");
        }
        SupplierChannel c = new SupplierChannel();
        applyChannel(c, req, true);
        channelService.save(c);
        return toConfigVO(c);
    }

    @Transactional
    public AdminSupplierConfigVO updateConfig(Long id, AdminSupplierConfigRequest req) {
        SupplierChannel c = requireChannel(id);
        applyChannel(c, req, false);
        channelService.updateById(c);
        return toConfigVO(c);
    }

    @Transactional
    public void deleteConfig(Long id) {
        requireChannel(id);
        skuMapService.remove(new LambdaQueryWrapper<SupplierSkuMap>().eq(SupplierSkuMap::getChannelId, id));
        channelService.removeById(id);
    }

    public Map<String, Object> testConfig(Long id) {
        SupplierChannel c = requireChannel(id);
        Map<String, String> cred = parseCred(c.getCredentialJson());
        boolean ok = StringUtils.hasText(c.getBaseUrl())
                && (StringUtils.hasText(cred.get("testUsername")) || StringUtils.hasText(cred.get("prodUsername")));
        if (!ok) {
            throw new BizException("配置不完整，无法测试");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置校验通过（未实际调用供应商接口）");
        result.put("environment", c.getEnvironment());
        return result;
    }

    @Transactional
    public AdminSupplierConfigVO switchEnvironment(Long id, String environment) {
        if (!StringUtils.hasText(environment)
                || (!"TEST".equalsIgnoreCase(environment) && !"PROD".equalsIgnoreCase(environment))) {
            throw new BizException("environment 仅支持 TEST/PROD");
        }
        SupplierChannel c = requireChannel(id);
        c.setEnvironment(environment.trim().toUpperCase());
        channelService.updateById(c);
        return toConfigVO(c);
    }

    public List<AdminSupplierMappingVO> listMappings() {
        return skuMapService.list(new LambdaQueryWrapper<SupplierSkuMap>().orderByDesc(SupplierSkuMap::getId))
                .stream().map(this::toMappingVO).toList();
    }

    @Transactional
    public AdminSupplierMappingVO createMapping(AdminSupplierMappingRequest req) {
        Long channelId = resolveChannelId(req);
        if (channelId == null) {
            throw new BizException("请指定 configId/channelId");
        }
        requireChannel(channelId);
        SupplierSkuMap map = new SupplierSkuMap();
        applyMapping(map, req, channelId);
        skuMapService.save(map);
        return toMappingVO(map);
    }

    @Transactional
    public AdminSupplierMappingVO updateMapping(Long id, AdminSupplierMappingRequest req) {
        SupplierSkuMap map = requireMapping(id);
        Long channelId = resolveChannelId(req);
        if (channelId != null) {
            requireChannel(channelId);
        } else {
            channelId = map.getChannelId();
        }
        applyMapping(map, req, channelId);
        skuMapService.updateById(map);
        return toMappingVO(map);
    }

    @Transactional
    public void deleteMapping(Long id) {
        requireMapping(id);
        skuMapService.removeById(id);
    }

    @Transactional
    public void batchDeleteMappings(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        skuMapService.removeByIds(ids);
    }

    public Map<String, Object> listTerminalPlans(Long configId, String type) {
        requireChannel(configId);
        Map<String, Object> result = new HashMap<>();
        result.put("configId", configId);
        result.put("type", type);
        result.put("plans", List.of());
        result.put("message", "供应商套餐列表未接入，返回空列表");
        return result;
    }

    public Map<String, Object> syncPreview(Long configId, String type) {
        requireChannel(configId);
        Map<String, Object> result = new HashMap<>();
        result.put("configId", configId);
        result.put("type", type);
        result.put("items", List.of());
        result.put("message", "同步预览未接入，返回空列表");
        return result;
    }

    public Map<String, Object> syncApply(Map<String, Object> body) {
        Long configId = body == null || body.get("configId") == null ? null : Long.valueOf(String.valueOf(body.get("configId")));
        if (configId != null) {
            requireChannel(configId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("applied", 0);
        result.put("message", "同步应用未接入（stub）");
        return result;
    }

    public Map<String, Object> planSyncPreview(Long configId, String type) {
        return syncPreview(configId, type);
    }

    public Map<String, Object> planSyncApply(Map<String, Object> body) {
        return syncApply(body);
    }

    public Map<String, Object> bulkPreview(Long configId, Map<String, ?> params) {
        requireChannel(configId);
        List<Map<String, Object>> options = new ArrayList<>();
        List<Product> products = productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, false)
                .orderByAsc(Product::getSortOrder)
                .last("LIMIT 50"));
        for (Product p : products) {
            List<ProductSku> skus = productSkuService.list(new LambdaQueryWrapper<ProductSku>()
                    .eq(ProductSku::getProductId, p.getId())
                    .orderByAsc(ProductSku::getSortOrder));
            for (ProductSku sku : skus) {
                Map<String, Object> row = new HashMap<>();
                row.put("productId", p.getId());
                row.put("skuId", sku.getId());
                row.put("spuCode", p.getSpuCode());
                row.put("skuCode", sku.getSkuCode());
                row.put("mapped", skuMapService.count(new LambdaQueryWrapper<SupplierSkuMap>()
                        .eq(SupplierSkuMap::getChannelId, configId)
                        .eq(SupplierSkuMap::getSkuId, sku.getId())) > 0);
                options.add(row);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("configId", configId);
        result.put("params", params);
        result.put("items", options);
        result.put("message", "批量映射预览基于本地商品 SKU");
        return result;
    }

    @Transactional
    public Map<String, Object> bulkApply(Map<String, Object> body) {
        if (body == null || body.get("configId") == null) {
            throw new BizException("缺少 configId");
        }
        Long configId = Long.valueOf(String.valueOf(body.get("configId")));
        requireChannel(configId);
        int applied = 0;
        Object itemsObj = body.get("items");
        if (itemsObj instanceof List<?> items) {
            for (Object item : items) {
                if (!(item instanceof Map<?, ?> map)) {
                    continue;
                }
                Long productId = map.get("productId") == null ? null : Long.valueOf(String.valueOf(map.get("productId")));
                Long skuId = map.get("skuId") == null ? null : Long.valueOf(String.valueOf(map.get("skuId")));
                String planId = map.get("planId") == null ? null : String.valueOf(map.get("planId"));
                if (skuId == null && productId == null) {
                    continue;
                }
                long exists = skuMapService.count(new LambdaQueryWrapper<SupplierSkuMap>()
                        .eq(SupplierSkuMap::getChannelId, configId)
                        .eq(skuId != null, SupplierSkuMap::getSkuId, skuId)
                        .eq(productId != null, SupplierSkuMap::getProductId, productId));
                if (exists > 0) {
                    continue;
                }
                SupplierSkuMap row = new SupplierSkuMap();
                row.setChannelId(configId);
                row.setProductId(productId);
                row.setSkuId(skuId);
                row.setExternalPlanId(planId);
                row.setAutoFulfill(true);
                skuMapService.save(row);
                applied++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("applied", applied);
        result.put("message", "批量映射已写入本地");
        return result;
    }

    private void applyChannel(SupplierChannel c, AdminSupplierConfigRequest req, boolean creating) {
        if (StringUtils.hasText(req.getCode())) {
            c.setCode(req.getCode().trim());
        } else if (creating || !StringUtils.hasText(c.getCode())) {
            c.setCode("TALK2ALL");
        }
        if (StringUtils.hasText(req.getName())) {
            c.setName(req.getName().trim());
        }
        if (StringUtils.hasText(req.getEnvironment())) {
            c.setEnvironment(req.getEnvironment().trim().toUpperCase());
        } else if (!StringUtils.hasText(c.getEnvironment())) {
            c.setEnvironment("TEST");
        }
        if (req.getBaseUrl() != null) {
            c.setBaseUrl(req.getBaseUrl());
        }
        if (req.getCallbackUrl() != null) {
            c.setCallbackUrl(req.getCallbackUrl());
        }
        if (req.getEnabled() != null) {
            c.setEnabled(req.getEnabled());
        } else if (c.getEnabled() == null) {
            c.setEnabled(true);
        }
        if (StringUtils.hasText(req.getCredentialJson())) {
            c.setCredentialJson(req.getCredentialJson().trim());
            return;
        }
        Map<String, String> cred = parseCred(c.getCredentialJson());
        if (req.getTestUsername() != null) {
            cred.put("testUsername", req.getTestUsername());
        }
        if (req.getTestApiKey() != null) {
            cred.put("testApiKey", req.getTestApiKey());
        }
        if (req.getProdUsername() != null) {
            cred.put("prodUsername", req.getProdUsername());
        }
        if (req.getProdApiKey() != null) {
            cred.put("prodApiKey", req.getProdApiKey());
        }
        if (req.getDescription() != null) {
            cred.put("description", req.getDescription());
        }
        try {
            c.setCredentialJson(MAPPER.writeValueAsString(cred));
        } catch (Exception e) {
            throw new BizException("凭证序列化失败");
        }
    }

    private void applyMapping(SupplierSkuMap map, AdminSupplierMappingRequest req, Long channelId) {
        map.setChannelId(channelId);
        if (req.getProductId() != null) {
            map.setProductId(req.getProductId());
        }
        if (req.getSkuId() != null) {
            map.setSkuId(req.getSkuId());
        }
        if (req.getExternalPlanId() != null) {
            map.setExternalPlanId(req.getExternalPlanId());
        } else if (req.getPlanId() != null) {
            map.setExternalPlanId(String.valueOf(req.getPlanId()));
        }
        if (req.getExternalName() != null) {
            map.setExternalName(req.getExternalName());
        }
        if (req.getAutoFulfill() != null) {
            map.setAutoFulfill(req.getAutoFulfill());
        } else if (req.getAutoDeliveryEnabled() != null) {
            map.setAutoFulfill(req.getAutoDeliveryEnabled());
        } else if (map.getAutoFulfill() == null) {
            map.setAutoFulfill(true);
        }
        Map<String, Object> extra = parseExtra(map.getExtraJson());
        if (req.getTerminal() != null) {
            extra.put("terminal", req.getTerminal());
        }
        if (req.getExtraJson() != null) {
            map.setExtraJson(req.getExtraJson());
        } else {
            try {
                map.setExtraJson(MAPPER.writeValueAsString(extra));
            } catch (Exception e) {
                map.setExtraJson("{}");
            }
        }
    }

    private Long resolveChannelId(AdminSupplierMappingRequest req) {
        if (req == null) {
            return null;
        }
        if (req.getChannelId() != null) {
            return req.getChannelId();
        }
        return req.getConfigId();
    }

    private SupplierChannel requireChannel(Long id) {
        SupplierChannel c = channelService.getById(id);
        if (c == null) {
            throw new BizException("供应商配置不存在");
        }
        return c;
    }

    private SupplierSkuMap requireMapping(Long id) {
        SupplierSkuMap m = skuMapService.getById(id);
        if (m == null) {
            throw new BizException("映射不存在");
        }
        return m;
    }

    private AdminSupplierConfigVO toConfigVO(SupplierChannel c) {
        Map<String, String> cred = parseCred(c.getCredentialJson());
        return AdminSupplierConfigVO.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .testUsername(cred.get("testUsername"))
                .testApiKey(cred.get("testApiKey"))
                .prodUsername(cred.get("prodUsername"))
                .prodApiKey(cred.get("prodApiKey"))
                .baseUrl(c.getBaseUrl())
                .environment(c.getEnvironment())
                .callbackUrl(c.getCallbackUrl())
                .enabled(c.getEnabled())
                .description(cred.get("description"))
                .credentialJson(c.getCredentialJson())
                .build();
    }

    private AdminSupplierMappingVO toMappingVO(SupplierSkuMap m) {
        Map<String, Object> extra = parseExtra(m.getExtraJson());
        Long planId = null;
        if (StringUtils.hasText(m.getExternalPlanId())) {
            try {
                planId = Long.valueOf(m.getExternalPlanId());
            } catch (Exception ignored) {
                // keep null
            }
        }
        return AdminSupplierMappingVO.builder()
                .id(m.getId())
                .configId(m.getChannelId())
                .channelId(m.getChannelId())
                .productId(m.getProductId())
                .skuId(m.getSkuId())
                .planId(planId)
                .planIdStr(m.getExternalPlanId())
                .externalPlanId(m.getExternalPlanId())
                .externalName(m.getExternalName())
                .terminal(extra.get("terminal") == null ? null : String.valueOf(extra.get("terminal")))
                .autoDeliveryEnabled(m.getAutoFulfill())
                .autoFulfill(m.getAutoFulfill())
                .build();
    }

    private Map<String, String> parseCred(String json) {
        if (!StringUtils.hasText(json)) {
            return new HashMap<>();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private Map<String, Object> parseExtra(String json) {
        if (!StringUtils.hasText(json)) {
            return new HashMap<>();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
