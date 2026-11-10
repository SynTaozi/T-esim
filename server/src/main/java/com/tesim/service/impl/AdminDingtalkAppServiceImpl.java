package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AdminDingtalkAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminDingtalkConfigRequest;
import com.tesim.dto.admin.AdminDingtalkConfigVO;
import com.tesim.dto.admin.AdminDingtalkLogVO;
import com.tesim.entity.SysDingtalkConfig;
import com.tesim.entity.SysDingtalkNotifyLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDingtalkAppServiceImpl implements AdminDingtalkAppService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysDingtalkConfigService configService;
    private final SysDingtalkNotifyLogService logService;
    private final DingtalkNotifyService dingtalkNotifyService;

    public List<AdminDingtalkConfigVO> listConfigs() {
        return configService.list(new LambdaQueryWrapper<SysDingtalkConfig>()
                        .orderByDesc(SysDingtalkConfig::getId))
                .stream().map(this::toVO).toList();
    }

    public AdminDingtalkConfigVO getConfig(Long id) {
        return toVO(require(id));
    }

    @Transactional
    public AdminDingtalkConfigVO createConfig(AdminDingtalkConfigRequest req) {
        validate(req);
        SysDingtalkConfig c = new SysDingtalkConfig();
        apply(c, req);
        configService.save(c);
        return toVO(c);
    }

    @Transactional
    public AdminDingtalkConfigVO updateConfig(Long id, AdminDingtalkConfigRequest req) {
        SysDingtalkConfig c = require(id);
        validate(req);
        apply(c, req);
        configService.updateById(c);
        return toVO(c);
    }

    @Transactional
    public void deleteConfig(Long id) {
        require(id);
        configService.removeById(id);
    }

    public Map<String, Object> testSend(Long id) {
        require(id);
        dingtalkNotifyService.testSend(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "测试消息已发送，请查看钉钉群");
        return result;
    }

    public PageResult<AdminDingtalkLogVO> pageLogs(String type, Integer page, Integer size) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? 20 : size;
        LambdaQueryWrapper<SysDingtalkNotifyLog> qw = new LambdaQueryWrapper<SysDingtalkNotifyLog>()
                .orderByDesc(SysDingtalkNotifyLog::getId);
        if (StringUtils.hasText(type)) {
            qw.eq(SysDingtalkNotifyLog::getEventType, type.trim());
        }
        Page<SysDingtalkNotifyLog> result = logService.page(new Page<>(p + 1L, s), qw);
        List<AdminDingtalkLogVO> content = result.getRecords().stream().map(l -> AdminDingtalkLogVO.builder()
                .id(l.getId())
                .configId(l.getConfigId())
                .eventType(l.getEventType())
                .type(l.getEventType())
                .title(l.getTitle())
                .content(l.getContent())
                .status(l.getStatus())
                .errorMessage(l.getErrorMessage())
                .createdAt(l.getCreatedAt() == null ? null : l.getCreatedAt().format(FMT))
                .build()).toList();
        return PageResult.of(content, result.getTotal(), p, s);
    }

    private void validate(AdminDingtalkConfigRequest req) {
        if (req == null || !StringUtils.hasText(req.getName()) || !StringUtils.hasText(req.getWebhookUrl())) {
            throw new BizException("名称和 Webhook 不能为空");
        }
    }

    private void apply(SysDingtalkConfig c, AdminDingtalkConfigRequest req) {
        c.setName(req.getName().trim());
        c.setWebhookUrl(req.getWebhookUrl().trim());
        c.setEnabled(req.getEnabled() == null || req.getEnabled());
        c.setDescription(req.getDescription());
    }

    private SysDingtalkConfig require(Long id) {
        SysDingtalkConfig c = configService.getById(id);
        if (c == null) {
            throw new BizException("钉钉配置不存在");
        }
        return c;
    }

    private AdminDingtalkConfigVO toVO(SysDingtalkConfig c) {
        return AdminDingtalkConfigVO.builder()
                .id(c.getId())
                .name(c.getName())
                .webhookUrl(c.getWebhookUrl())
                .enabled(c.getEnabled())
                .description(c.getDescription())
                .build();
    }
}
