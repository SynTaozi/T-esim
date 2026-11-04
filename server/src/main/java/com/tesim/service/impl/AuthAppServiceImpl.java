package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AuthAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tesim.common.BizException;
import com.tesim.dto.admin.AdminAccountUpdateRequest;
import com.tesim.dto.admin.AdminSessionVO;
import com.tesim.dto.auth.AuthTokenVO;
import com.tesim.dto.auth.ChangePasswordRequest;
import com.tesim.dto.auth.LoginRequest;
import com.tesim.dto.auth.MemberVO;
import com.tesim.dto.auth.RegisterRequest;
import com.tesim.dto.auth.ResetPasswordRequest;
import com.tesim.config.TesimMailProperties;
import com.tesim.dto.auth.SendCodeVO;
import com.tesim.dto.auth.UpdateProfileRequest;
import com.tesim.entity.Member;
import com.tesim.mail.MailBizTypes;
import com.tesim.mail.MailMessage;
import com.tesim.mail.MailPurposes;
import com.tesim.security.JwtUtil;
import com.tesim.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthAppServiceImpl implements AuthAppService {

    private final MemberService memberService;
    private final VerificationCodeService codeService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MailSendService mailSendService;
    private final TesimMailProperties mailProperties;

    public SendCodeVO sendRegisterCode(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BizException("邮箱不能为空");
        }
        String em = email.trim().toLowerCase();
        long exists = memberService.count(new LambdaQueryWrapper<Member>().eq(Member::getEmail, em));
        if (exists > 0) {
            throw new BizException("邮箱已被注册");
        }
        String code = codeService.send("REGISTER", em);
        sendAuthCodeMail(MailBizTypes.AUTH_REGISTER_CODE, em, code, "【Tesim】注册验证码");
        return new SendCodeVO(true, mailProperties.isExposeDevCode() ? code : null);
    }

    public SendCodeVO sendResetCode(String email) {
        String em = email.trim().toLowerCase();
        Member m = memberService.getOne(new LambdaQueryWrapper<Member>().eq(Member::getEmail, em));
        if (m == null) {
            throw new BizException("邮箱未注册");
        }
        String code = codeService.send("RESET", em);
        sendAuthCodeMail(MailBizTypes.AUTH_RESET_CODE, em, code, "【Tesim】重置密码验证码");
        return new SendCodeVO(true, mailProperties.isExposeDevCode() ? code : null);
    }

    private void sendAuthCodeMail(String templateCode, String email, String code, String fallbackSubject) {
        Map<String, String> vars = new HashMap<>();
        // 模板同时存在 {{code}} / ${CODE}、{{validityMinutes}} / ${VALIDITY_MINUTES}
        vars.put("code", code);
        vars.put("email", email);
        vars.put("validityMinutes", "10");
        vars.put("expireMinutes", "10");
        mailSendService.sendAsync(MailMessage.builder()
                .to(email)
                .purpose(MailPurposes.GENERAL)
                .bizType(templateCode)
                .bizId(email)
                .templateCode(templateCode)
                .locale("zh")
                .variables(vars)
                .subject(fallbackSubject)
                .htmlBody("<p>您的验证码是：<strong>" + code + "</strong>（10 分钟内有效）</p>")
                .build());
    }

    @Transactional
    public AuthTokenVO register(RegisterRequest req) {
        codeService.verifyOrThrow("REGISTER", req.getEmail(), req.getVerificationCode());
        if (memberService.count(new LambdaQueryWrapper<Member>().eq(Member::getUsername, req.getUsername())) > 0) {
            throw new BizException("用户名已存在");
        }
        if (memberService.count(new LambdaQueryWrapper<Member>().eq(Member::getEmail, req.getEmail())) > 0) {
            throw new BizException("邮箱已被注册");
        }
        Member m = new Member();
        m.setUsername(req.getUsername().trim());
        m.setEmail(req.getEmail().trim().toLowerCase());
        m.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        m.setPhone(req.getPhone());
        m.setStatus("ACTIVE");
        m.setRole("USER");
        m.setDeleted(false);
        memberService.save(m);
        return loginResult(m);
    }

    public AuthTokenVO login(LoginRequest req) {
        return loginResult(authenticate(req));
    }

    public AdminSessionVO adminLogin(LoginRequest req) {
        Member m = authenticate(req);
        if (!"ADMIN".equalsIgnoreCase(m.getRole())) {
            throw new BizException(403, "非管理员账号无权登录");
        }
        return toAdminSession(m);
    }

    public AdminSessionVO adminMe() {
        Member m = requireAdminMember();
        return toAdminSession(m);
    }

    @Transactional
    public AdminSessionVO updateAdminAccount(AdminAccountUpdateRequest req) {
        Member m = requireAdminMember();
        if (!passwordEncoder.matches(req.getCurrentPassword(), m.getPasswordHash())) {
            throw new BizException("当前密码不正确");
        }
        if (StringUtils.hasText(req.getUsername()) && !req.getUsername().trim().equals(m.getUsername())) {
            String username = req.getUsername().trim();
            long dup = memberService.count(new LambdaQueryWrapper<Member>()
                    .eq(Member::getUsername, username)
                    .ne(Member::getId, m.getId()));
            if (dup > 0) {
                throw new BizException("用户名已存在");
            }
            m.setUsername(username);
        }
        if (StringUtils.hasText(req.getNewPassword())) {
            if (req.getCurrentPassword().equals(req.getNewPassword())) {
                throw new BizException("新密码不能与当前密码相同");
            }
            m.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        }
        memberService.updateById(m);
        return toAdminSession(m);
    }

    private Member authenticate(LoginRequest req) {
        String account = req.getUsername().trim();
        Member m = memberService.getOne(new LambdaQueryWrapper<Member>()
                .and(w -> w.eq(Member::getUsername, account).or().eq(Member::getEmail, account)));

        if (m == null || !passwordEncoder.matches(req.getPassword(), m.getPasswordHash())) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (!"ACTIVE".equalsIgnoreCase(m.getStatus())) {
            throw new BizException("账号不可用");
        }
        return m;
    }

    private Member requireAdminMember() {
        Long id = SecurityUtils.requireMemberId();
        Member m = memberService.getById(id);
        if (m == null) {
            throw new BizException(401, "请先登录");
        }
        if (!"ADMIN".equalsIgnoreCase(m.getRole())) {
            throw new BizException(403, "需要管理员权限");
        }
        return m;
    }

    private AdminSessionVO toAdminSession(Member m) {
        String token = jwtUtil.createToken(m.getId(), m.getUsername(), m.getRole());
        return AdminSessionVO.builder()
                .token(token)
                .id(m.getId())
                .username(m.getUsername())
                .email(m.getEmail())
                .role(m.getRole())
                .fullName(m.getFullName())
                .build();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        codeService.verifyOrThrow("RESET", req.getEmail(), req.getVerificationCode());
        Member m = memberService.getOne(new LambdaQueryWrapper<Member>().eq(Member::getEmail, req.getEmail().trim()));
        if (m == null) {
            throw new BizException("邮箱未注册");
        }
        m.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        memberService.updateById(m);
    }

    public MemberVO profile() {
        Long id = SecurityUtils.requireMemberId();
        Member m = memberService.getById(id);
        if (m == null) {
            throw new BizException(401, "请先登录");
        }
        return toMemberVO(m);
    }

    @Transactional
    public MemberVO updateProfile(UpdateProfileRequest req) {
        Long id = SecurityUtils.requireMemberId();
        Member m = memberService.getById(id);
        if (m == null) {
            throw new BizException(401, "请先登录");
        }
        if (req.getFullName() != null) {
            m.setFullName(req.getFullName());
        }
        if (req.getPhone() != null) {
            m.setPhone(req.getPhone());
        }
        if (req.getEmail() != null) {
            m.setEmail(req.getEmail().trim().toLowerCase());
        }
        memberService.updateById(m);
        return toMemberVO(m);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        Long id = SecurityUtils.requireMemberId();
        Member m = memberService.getById(id);
        if (m == null) {
            throw new BizException(401, "请先登录");
        }
        if (!passwordEncoder.matches(req.getOldPassword(), m.getPasswordHash())) {
            throw new BizException("原密码不正确");
        }
        if (req.getOldPassword().equals(req.getNewPassword())) {
            throw new BizException("新密码不能与原密码相同");
        }
        m.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        memberService.updateById(m);
    }

    private AuthTokenVO loginResult(Member m) {
        String token = jwtUtil.createToken(m.getId(), m.getUsername(), m.getRole());
        return new AuthTokenVO(token, toMemberVO(m));
    }

    private MemberVO toMemberVO(Member m) {
        return MemberVO.builder()
                .id(m.getId())
                .username(m.getUsername())
                .email(m.getEmail())
                .phone(m.getPhone())
                .fullName(m.getFullName())
                .role(m.getRole())
                .status(m.getStatus())
                .build();
    }
}
