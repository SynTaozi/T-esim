package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AdminEmailAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminEmailAccountRequest;
import com.tesim.dto.admin.AdminEmailAccountVO;
import com.tesim.dto.admin.AdminEmailTemplateRequest;
import com.tesim.dto.admin.AdminEmailTemplateVO;
import com.tesim.entity.I18nSysEmailTemplate;
import com.tesim.entity.SalesOrderNotice;
import com.tesim.entity.SysEmailAccount;
import com.tesim.entity.SysEmailTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminEmailAppServiceImpl implements AdminEmailAppService {

    private final SysEmailAccountService emailAccountService;
    private final SysEmailTemplateService emailTemplateService;
    private final I18nSysEmailTemplateService i18nEmailTemplateService;
    private final SalesOrderNoticeService noticeService;

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
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置校验通过（未实际发送测试邮件）");
        result.put("accountId", a.getId());
        result.put("emailAddress", a.getEmailAddress());
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

    @Transactional
    public Map<String, Object> sendEmail(Long accountId, Map<String, Object> body) {
        SysEmailAccount a = requireAccount(accountId);
        String to = body == null ? null : str(body.get("to"));
        String subject = body == null ? null : str(body.get("subject"));
        String content = body == null ? null : str(body.get("body"));
        if (!StringUtils.hasText(to) || !StringUtils.hasText(content)) {
            throw new BizException("收件人和正文不能为空");
        }
        SalesOrderNotice notice = new SalesOrderNotice();
        notice.setOrderId(0L);
        notice.setNoticeType("ADMIN_EMAIL");
        notice.setSubject(StringUtils.hasText(subject) ? subject : "管理员邮件");
        notice.setContent("from=" + a.getEmailAddress() + "; to=" + to + "; body=" + content);
        notice.setSentAt(LocalDateTime.now());
        noticeService.save(notice);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已记录发送通知（邮件通道未接入）");
        result.put("noticeId", notice.getId());
        return result;
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
        return emailTemplateService.list(new LambdaQueryWrapper<SysEmailTemplate>()
                        .orderByAsc(SysEmailTemplate::getId))
                .stream().map(this::toTemplateVO).toList();
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
        a.setPurpose(req.getPurpose());
        a.setEnabled(req.getEnabled() == null || req.getEnabled());
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
            row.setName(name);
            row.setSubject(subject);
            row.setBody(body);
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
                .eq(I18nSysEmailTemplate::getLocale, "zh"));
        I18nSysEmailTemplate en = i18nEmailTemplateService.getOne(new LambdaQueryWrapper<I18nSysEmailTemplate>()
                .eq(I18nSysEmailTemplate::getTemplateId, t.getId())
                .eq(I18nSysEmailTemplate::getLocale, "en"));
        return AdminEmailTemplateVO.builder()
                .id(t.getId())
                .code(t.getCode())
                .name(zh != null ? zh.getName() : t.getCode())
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
