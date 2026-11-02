package com.tesim.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tesim.common.ApiResponse;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.phone.PhoneCatalogPageVO;
import com.tesim.dto.phone.PhoneDeviceVO;
import com.tesim.dto.phone.PhoneNumberVO;
import com.tesim.dto.phone.PhoneStatsVO;
import com.tesim.dto.phone.UserPhoneVO;
import com.tesim.entity.MemberSubscription;
import com.tesim.security.SecurityUtils;
import com.tesim.service.MemberSubscriptionService;
import com.tesim.service.PhoneCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class PhoneNumberController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MemberSubscriptionService memberSubscriptionService;
    private final PhoneCatalogService phoneCatalogService;

    @GetMapping("/phone-numbers")
    public ApiResponse<PhoneCatalogPageVO> catalog(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(phoneCatalogService.catalog(region, page, size));
    }

    @GetMapping("/phone-numbers/regions")
    public ApiResponse<List<String>> regions() {
        return ApiResponse.ok(phoneCatalogService.regions());
    }

    @GetMapping("/phone-numbers/devices")
    public ApiResponse<List<PhoneDeviceVO>> devices() {
        return ApiResponse.ok(phoneCatalogService.devices());
    }

    @GetMapping("/phone-numbers/{id}")
    public ApiResponse<PhoneNumberVO> catalogDetail(@PathVariable Long id) {
        return ApiResponse.ok(phoneCatalogService.detail(id));
    }

    @GetMapping("/user/phone-numbers")
    public ApiResponse<PageResult<UserPhoneVO>> myPhones(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Long memberId = SecurityUtils.requireMemberId();
        int p = page == null ? 0 : page;
        int s = size == null ? 10 : size;
        Page<MemberSubscription> result = memberSubscriptionService.page(new Page<>(p + 1L, s),
                new LambdaQueryWrapper<MemberSubscription>()
                        .eq(MemberSubscription::getMemberId, memberId)
                        .orderByDesc(MemberSubscription::getCreatedAt));
        List<UserPhoneVO> content = result.getRecords().stream().map(this::toPhone).toList();
        return ApiResponse.ok(PageResult.of(content, result.getTotal(), p, s));
    }

    @GetMapping("/user/phone-numbers/stats")
    public ApiResponse<PhoneStatsVO> stats() {
        Long memberId = SecurityUtils.requireMemberId();
        List<MemberSubscription> list = memberSubscriptionService.list(new LambdaQueryWrapper<MemberSubscription>()
                .eq(MemberSubscription::getMemberId, memberId));
        LocalDateTime soon = LocalDateTime.now().plusDays(7);
        List<UserPhoneVO> expiring = list.stream()
                .filter(x -> "ACTIVE".equalsIgnoreCase(x.getStatus())
                        && x.getExpiresAt() != null
                        && !x.getExpiresAt().isAfter(soon))
                .map(this::toPhone)
                .toList();
        long active = list.stream().filter(x -> "ACTIVE".equalsIgnoreCase(x.getStatus())).count();
        long expired = list.stream().filter(x -> "EXPIRED".equalsIgnoreCase(x.getStatus())).count();
        BigDecimal spent = list.stream()
                .map(x -> x.getPurchasePrice() == null ? BigDecimal.ZERO : x.getPurchasePrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ApiResponse.ok(PhoneStatsVO.builder()
                .totalCount(list.size())
                .activeCount(active)
                .expiredCount(expired)
                .totalSpent(spent)
                .expiringCount(expiring.size())
                .expiringNumbers(expiring)
                .build());
    }

    @GetMapping("/user/phone-numbers/{id}")
    public ApiResponse<UserPhoneVO> myPhone(@PathVariable Long id) {
        return ApiResponse.ok(toPhone(requireOwned(id)));
    }

    @PostMapping("/user/phone-numbers/{id}/renew")
    public ApiResponse<UserPhoneVO> renew(@PathVariable Long id) {
        requireOwned(id);
        throw new BizException("续费请通过下单流程完成");
    }

    @PostMapping("/user/phone-numbers/{id}/cancel")
    public ApiResponse<UserPhoneVO> cancel(@PathVariable Long id) {
        MemberSubscription sub = requireOwned(id);
        sub.setStatus("CANCELLED");
        memberSubscriptionService.updateById(sub);
        return ApiResponse.ok(toPhone(sub));
    }

    private MemberSubscription requireOwned(Long id) {
        MemberSubscription sub = memberSubscriptionService.getById(id);
        if (sub == null || !Objects.equals(sub.getMemberId(), SecurityUtils.requireMemberId())) {
            throw new BizException("号码不存在");
        }
        return sub;
    }

    private UserPhoneVO toPhone(MemberSubscription s) {
        return UserPhoneVO.builder()
                .id(s.getId())
                .phoneNumber(s.getMsisdn())
                .countryCode(s.getCountryCode())
                .country(s.getCountryCode())
                .carrier(s.getCarrier())
                .purchasePrice(s.getPurchasePrice())
                .renewalPrice(s.getRenewalPrice())
                .expiryDate(s.getExpiresAt() == null ? null : FMT.format(s.getExpiresAt()))
                .status(s.getStatus())
                .notes(s.getPlanSnapshot())
                .createdAt(s.getCreatedAt() == null ? null : FMT.format(s.getCreatedAt()))
                .updatedAt(s.getUpdatedAt() == null ? null : FMT.format(s.getUpdatedAt()))
                .build();
    }
}
