package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AdminEmailAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tesim.dto.admin.AdminEmailAccountRequest;
import com.tesim.dto.admin.AdminEmailAccountVO;
import com.tesim.dto.admin.AdminEmailSendLogVO;
import com.tesim.dto.admin.AdminEmailTemplateRequest;
import com.tesim.dto.admin.AdminEmailTemplateVO;
import com.tesim.entity.I18nSysEmailTemplate;
import com.tesim.entity.SysEmailAccount;
import com.tesim.entity.SysEmailSendLog;
import com.tesim.entity.SysEmailTemplate;
import com.tesim.mail.MailBizTypes;
import com.tesim.mail.MailMessage;
import com.tesim.mail.MailPurposes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminEmailAppServiceImpl implements AdminEmailAppService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysEmailAccountService emailAccountService;
    private final SysEmailTemplateService emailTemplateService;
    private final I18nSysEmailTemplateService i18nEmailTemplateService;
    private final SysEmailSendLogService sendLogService;
    private final MailSendService mailSendService;

    public List<AdminEmailAccountVO> listAccounts() {
        return emailAccountService.list(new LambdaQueryWrapper<SysEmailAccount>()
                        .orderByDesc(SysEmailAccount::getId))
                .stream().map(this::toAccountVO).toList();
    }

    public AdminEmailAccountVO getAccount(Long id) {
        return toAccountVO(requireAccount(id));
    }

    @Transactional
    public AdminEmailAccountVO createAccount(AdminEmailAccountRequest req) {
        validateAccount(req, true);
        SysEmailAccount a = new SysEmailAccount();
        applyAccount(a, req, true);
        emailAccountService.save(a);
        return toAccountVO(a);
    }

    @Transactional
    public AdminEmailAccountVO updateAccount(Long id, AdminEmailAccountRequest req) {
        SysEmailAccount a = requireAccount(id);
        validateAccount(req, false);
        applyAccount(a, req, false);
        emailAccountService.updateById(a);
        return toAccountVO(a);
    }

    @Transactional
    public void deleteAccount(Long id) {
        requireAccount(id);
        emailAccountService.removeById(id);
    }

    public Map<String, Object> testAccount(Long id) {
        SysEmailAccount a = requireAccount(id);
        if (!StringUtils.hasText(a.getSmtpHost()) || a.getSmtpPort() == null
                || !StringUtils.hasText(a.getEmailAddress())
                || !StringUtils.hasText(a.getUsername())) {
            throw new BizException("邮箱账户配置不完整，无法测试");
        }
        if (!Boolean.TRUE.equals(a.getEnabled())) {
            throw new BizException("请先启用该邮箱账户");
        }
        Long logId = mailSendService.sendSync(MailMessage.builder()
                .accountId(a.getId())
                .purpose(StringUtils.hasText(a.getPurpose()) ? a.getPurpose() : MailPurposes.GENERAL)
                .bizType(MailBizTypes.ADMIN_TEST)
                .bizId(String.valueOf(a.getId()))
                .to(a.getEmailAddress())
                .subject("【Tesim】SMTP 测试邮件")
                .htmlBody("<p>这是一封来自 Tesim 后台的 SMTP 测试邮件。</p><p>账户：" + a.getName() + "</p>")
                .build());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "测试邮件已发送到 " + a.getEmailAddress());
        result.put("accountId", a.getId());
        result.put("emailAddress", a.getEmailAddress());
        result.put("logId", logId);
        return result;
    }

    public PageResult<Map<String, Object>> listEmails(Long accountId, String folderType, Integer start, Integer limit) {
        requireAccount(accountId);
        int page = start == null || start < 0 ? 0 : start;
        int size = limit == null || limit < 1 ? 20 : limit;
        return PageResult.empty(page, size);
    }

    public Map<String, Object> getEmail(Long accountId, String messageId, String folderType) {
        requireAccount(accountId);
        Map<String, Object> result = new HashMap<>();
        result.put("messageId", messageId);
        result.put("folderType", folderType);
        result.put("subject", null);
        result.put("body", null);
        return result;
    }

    public Map<String, Object> sendEmail(Long accountId, Map<String, Object> body) {
        SysEmailAccount a = requireAccount(accountId);
        String to = body == null ? null : str(body.get("to"));
        String subject = body == null ? null : str(body.get("subject"));
        String content = body == null ? null : str(body.get("body"));
        if (!StringUtils.hasText(to) || !StringUtils.hasText(content)) {
            throw new BizException("收件人和正文不能为空");
        }
        if (!Boolean.TRUE.equals(a.getEnabled())) {
            throw new BizException("请先启用该邮箱账户");
        }
        Long logId = mailSendService.sendAsync(MailMessage.builder()
                .accountId(a.getId())
                .purpose(StringUtils.hasText(a.getPurpose()) ? a.getPurpose() : MailPurposes.GENERAL)
                .bizType(MailBizTypes.ADMIN_MANUAL)
                .bizId(String.valueOf(a.getId()))
                .to(to.trim())
                .subject(StringUtils.hasText(subject) ? subject.trim() : "管理员邮件")
                .htmlBody(content.contains("<") ? content : "<pre style=\"white-space:pre-wrap;font-family:inherit\">"
                        + content.replace("&", "&amp;").replace("<", "&lt;") + "</pre>")
                .build());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "邮件已加入发送队列");
        result.put("logId", logId);
        return result;
    }

    public PageResult<AdminEmailSendLogVO> pageSendLogs(String status, String bizType, String toEmail,
                                                        Integer page, Integer size) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? 20 : size;
        LambdaQueryWrapper<SysEmailSendLog> qw = new LambdaQueryWrapper<SysEmailSendLog>()
                .orderByDesc(SysEmailSendLog::getId);
        if (StringUtils.hasText(status)) {
            qw.eq(SysEmailSendLog::getStatus, status.trim().toUpperCase());
        }
        if (StringUtils.hasText(bizType)) {
            qw.eq(SysEmailSendLog::getBizType, bizType.trim());
        }
        if (StringUtils.hasText(toEmail)) {
            qw.like(SysEmailSendLog::getToEmail, toEmail.trim());
        }
        Page<SysEmailSendLog> result = sendLogService.page(new Page<>(p + 1L, s), qw);
        List<AdminEmailSendLogVO> content = result.getRecords().stream().map(this::toSendLogVO).toList();
        return PageResult.of(content, result.getTotal(), p, s);
    }

    private AdminEmailSendLogVO toSendLogVO(SysEmailSendLog row) {
        return AdminEmailSendLogVO.builder()
                .id(row.getId())
                .accountId(row.getAccountId())
                .purpose(row.getPurpose())
                .bizType(row.getBizType())
                .bizId(row.getBizId())
                .toEmail(row.getToEmail())
                .subject(row.getSubject())
                .body(row.getBody())
                .status(row.getStatus())
                .errorMessage(row.getErrorMessage())
                .attemptCount(row.getAttemptCount())
                .sentAt(format(row.getSentAt()))
                .createdAt(format(row.getCreatedAt()))
                .build();
    }

    private static String format(LocalDateTime t) {
        return t == null ? null : FMT.format(t);
    }

    public Map<String, Object> markRead(Long accountId, String messageId) {
        requireAccount(accountId);
        return Map.of("success", true, "messageId", messageId);
    }

    public Map<String, Object> deleteEmail(Long accountId, String messageId) {
        requireAccount(accountId);
        return Map.of("success", true, "messageId", messageId);
    }

    public List<AdminEmailTemplateVO> listTemplates() {
        List<SysEmailTemplate> templates = emailTemplateService.list(new LambdaQueryWrapper<SysEmailTemplate>()
                .orderByAsc(SysEmailTemplate::getId));
        if (templates.isEmpty()) {
            return List.of();
        }
        List<Long> ids = templates.stream().map(SysEmailTemplate::getId).toList();
        List<I18nSysEmailTemplate> i18nRows = i18nEmailTemplateService.list(new LambdaQueryWrapper<I18nSysEmailTemplate>()
                .in(I18nSysEmailTemplate::getTemplateId, ids));
        Map<Long, I18nSysEmailTemplate> zhMap = new HashMap<>();
        Map<Long, I18nSysEmailTemplate> enMap = new HashMap<>();
        for (I18nSysEmailTemplate row : i18nRows) {
            if ("zh".equalsIgnoreCase(row.getLocale())) {
                zhMap.put(row.getTemplateId(), row);
            } else if ("en".equalsIgnoreCase(row.getLocale())) {
                enMap.put(row.getTemplateId(), row);
            }
        }
        return templates.stream()
                .map(t -> toTemplateVO(t, zhMap.get(t.getId()), enMap.get(t.getId())))
                .toList();
    }

    public AdminEmailTemplateVO getTemplate(Long id) {
        return toTemplateVO(requireTemplate(id));
    }

    @Transactional
    public AdminEmailTemplateVO updateTemplate(Long id, AdminEmailTemplateRequest req) {
        SysEmailTemplate t = requireTemplate(id);
        if (req.getEnabled() != null) {
            t.setEnabled(req.getEnabled());
        }
        if (req.getDescription() != null) {
            t.setDescription(req.getDescription());
        }
        emailTemplateService.updateById(t);
        upsertTemplateI18n(id, "zh", req.getName(), req.getSubject(), req.getBody());
        upsertTemplateI18n(id, "en", req.getNameEn(), req.getSubjectEn(), req.getBodyEn());
        return toTemplateVO(t);
    }

    @Transactional
    public AdminEmailTemplateVO updateTemplateEnabled(Long id, Boolean enabled) {
        SysEmailTemplate t = requireTemplate(id);
        t.setEnabled(Boolean.TRUE.equals(enabled));
        emailTemplateService.updateById(t);
        return toTemplateVO(t);
    }

    public Map<String, Object> previewTemplate(Long id) {
        AdminEmailTemplateVO vo = toTemplateVO(requireTemplate(id));
        Map<String, Object> result = new HashMap<>();
        result.put("id", vo.getId());
        result.put("code", vo.getCode());
        result.put("subject", vo.getSubject());
        result.put("body", vo.getBody());
        result.put("subjectEn", vo.getSubjectEn());
        result.put("bodyEn", vo.getBodyEn());
        return result;
    }

    private void validateAccount(AdminEmailAccountRequest req, boolean creating) {
        if (req == null) {
            throw new BizException("请求体不能为空");
        }
        if (!StringUtils.hasText(req.getName())
                || !StringUtils.hasText(req.getEmailAddress())
                || !StringUtils.hasText(req.getUsername())
                || !StringUtils.hasText(req.getSmtpHost())
                || req.getSmtpPort() == null) {
            throw new BizException("请填写名称、邮箱、用户名、SMTP 主机和端口");
        }
        if (creating && !StringUtils.hasText(req.getPassword())) {
            throw new BizException("请填写密码");
        }
        String purpose = normalizePurpose(req.getPurpose());
        if (purpose == null) {
            throw new BizException("请选择用途：GENERAL / SHIPPING / PAYMENT / SUPPORT");
        }
        req.setPurpose(purpose);
    }

    private void applyAccount(SysEmailAccount a, AdminEmailAccountRequest req, boolean creating) {
        a.setName(req.getName().trim());
        a.setEmailAddress(req.getEmailAddress().trim());
        a.setUsername(req.getUsername().trim());
        if (StringUtils.hasText(req.getPassword())) {
            a.setPassword(req.getPassword());
        } else if (creating) {
            a.setPassword("");
        }
        a.setSmtpHost(req.getSmtpHost().trim());
        a.setSmtpPort(req.getSmtpPort());
        a.setImapHost(req.getImapHost());
        a.setImapPort(req.getImapPort());
        // DB purpose NOT NULL；未传时默认 GENERAL，避免插入失败却被前端当成成功
        String purpose = normalizePurpose(req.getPurpose());
        a.setPurpose(purpose != null ? purpose : MailPurposes.GENERAL);
        a.setEnabled(req.getEnabled() == null || req.getEnabled());
    }

    private static String normalizePurpose(String purpose) {
        if (!StringUtils.hasText(purpose)) {
            return MailPurposes.GENERAL;
        }
        String p = purpose.trim().toUpperCase();
        return switch (p) {
            case MailPurposes.GENERAL, MailPurposes.SHIPPING, MailPurposes.PAYMENT, MailPurposes.SUPPORT -> p;
            default -> null;
        };
    }

    private SysEmailAccount requireAccount(Long id) {
        SysEmailAccount a = emailAccountService.getById(id);
        if (a == null) {
            throw new BizException("邮箱账户不存在");
        }
        return a;
    }

    private SysEmailTemplate requireTemplate(Long id) {
        SysEmailTemplate t = emailTemplateService.getById(id);
        if (t == null) {
            throw new BizException("邮件模板不存在");
        }
        return t;
    }

    private void upsertTemplateI18n(Long templateId, String locale, String name, String subject, String body) {
        if (!StringUtils.hasText(name) && subject == null && body == null) {
            return;
        }
        I18nSysEmailTemplate existing = i18nEmailTemplateService.getOne(new LambdaQueryWrapper<I18nSysEmailTemplate>()
                .eq(I18nSysEmailTemplate::getTemplateId, templateId)
                .eq(I18nSysEmailTemplate::getLocale, locale));
        if (existing == null) {
            I18nSysEmailTemplate row = new I18nSysEmailTemplate();
            row.setTemplateId(templateId);
            row.setLocale(locale);
            row.setName(StringUtils.hasText(name) ? name.trim() : ("zh".equals(locale) ? "邮件模板" : "Email template"));
            row.setSubject(subject == null ? "" : subject);
            row.setBody(body == null ? "" : body);
            i18nEmailTemplateService.save(row);
        } else {
            if (StringUtils.hasText(name)) {
                existing.setName(name.trim());
            }
            if (subject != null) {
                existing.setSubject(subject);
            }
            if (body != null) {
                existing.setBody(body);
            }
            i18nEmailTemplateService.update(existing, new LambdaQueryWrapper<I18nSysEmailTemplate>()
                    .eq(I18nSysEmailTemplate::getTemplateId, templateId)
                    .eq(I18nSysEmailTemplate::getLocale, locale));
        }
    }

    private AdminEmailAccountVO toAccountVO(SysEmailAccount a) {
        return AdminEmailAccountVO.builder()
                .id(a.getId())
                .name(a.getName())
                .emailAddress(a.getEmailAddress())
                .username(a.getUsername())
                .smtpHost(a.getSmtpHost())
                .smtpPort(a.getSmtpPort())
                .imapHost(a.getImapHost())
                .imapPort(a.getImapPort())
                .purpose(a.getPurpose())
                .enabled(a.getEnabled())
                .build();
    }

    private AdminEmailTemplateVO toTemplateVO(SysEmailTemplate t) {
        I18nSysEmailTemplate zh = i18nEmailTemplateService.getOne(new LambdaQueryWrapper<I18nSysEmailTemplate>()
                .eq(I18nSysEmailTemplate::getTemplateId, t.getId())
                .eq(I18nSysEmailTemplate::getLocale, "zh")
                .last("LIMIT 1"));
        I18nSysEmailTemplate en = i18nEmailTemplateService.getOne(new LambdaQueryWrapper<I18nSysEmailTemplate>()
                .eq(I18nSysEmailTemplate::getTemplateId, t.getId())
                .eq(I18nSysEmailTemplate::getLocale, "en")
                .last("LIMIT 1"));
        return toTemplateVO(t, zh, en);
    }

    private AdminEmailTemplateVO toTemplateVO(SysEmailTemplate t, I18nSysEmailTemplate zh, I18nSysEmailTemplate en) {
        return AdminEmailTemplateVO.builder()
                .id(t.getId())
                .code(t.getCode())
                .name(zh != null && StringUtils.hasText(zh.getName()) ? zh.getName() : t.getCode())
                .nameEn(en != null ? en.getName() : null)
                .subject(zh != null ? zh.getSubject() : null)
                .subjectEn(en != null ? en.getSubject() : null)
                .body(zh != null ? zh.getBody() : null)
                .bodyEn(en != null ? en.getBody() : null)
                .enabled(t.getEnabled())
                .description(t.getDescription())
                .build();
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
