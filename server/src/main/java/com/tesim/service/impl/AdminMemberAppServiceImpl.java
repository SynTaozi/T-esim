package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AdminMemberAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminMemberVO;
import com.tesim.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminMemberAppServiceImpl implements AdminMemberAppService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Set<String> STATUSES = Set.of("ACTIVE", "INACTIVE", "BANNED");

    private final MemberService memberService;

    public PageResult<AdminMemberVO> page(Integer page, Integer size, String keyword, String status) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? 10 : size;
        LambdaQueryWrapper<Member> qw = new LambdaQueryWrapper<Member>()
                .eq(Member::getDeleted, false)
                .ne(Member::getRole, "ADMIN")
                .orderByDesc(Member::getCreatedAt);
        if (StringUtils.hasText(status)) {
            qw.eq(Member::getStatus, status.trim().toUpperCase());
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(Member::getUsername, kw)
                    .or().like(Member::getEmail, kw)
                    .or().like(Member::getPhone, kw));
        }
        Page<Member> result = memberService.page(new Page<>(p + 1L, s), qw);
        List<AdminMemberVO> content = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(content, result.getTotal(), p, s);
    }

    public AdminMemberVO get(Long id) {
        return toVO(require(id));
    }

    @Transactional
    public AdminMemberVO updateStatus(Long id, String status) {
        if (!StringUtils.hasText(status) || !STATUSES.contains(status.trim().toUpperCase())) {
            throw new BizException("无效状态，允许: ACTIVE/INACTIVE/BANNED");
        }
        Member m = require(id);
        if ("ADMIN".equalsIgnoreCase(m.getRole())) {
            throw new BizException("不能修改管理员状态");
        }
        m.setStatus(status.trim().toUpperCase());
        memberService.updateById(m);
        return toVO(m);
    }

    private Member require(Long id) {
        Member m = memberService.getById(id);
        if (m == null || Boolean.TRUE.equals(m.getDeleted())) {
            throw new BizException("用户不存在");
        }
        return m;
    }

    private AdminMemberVO toVO(Member m) {
        return AdminMemberVO.builder()
                .id(m.getId())
                .username(m.getUsername())
                .email(m.getEmail())
                .phone(m.getPhone())
                .fullName(m.getFullName())
                .role(m.getRole())
                .status(m.getStatus())
                .createdAt(m.getCreatedAt() == null ? null : m.getCreatedAt().format(FMT))
                .build();
    }
}
