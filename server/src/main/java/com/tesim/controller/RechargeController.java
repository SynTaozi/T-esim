package com.tesim.controller;

import com.tesim.common.ApiResponse;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.recharge.BalanceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * Wallet/recharge was removed from Tesim. Endpoints remain so old front calls get a clear response.
 */
@RestController
@RequiredArgsConstructor
public class RechargeController {

    @GetMapping({"/wallet/balance", "/balance", "/member/balance"})
    public ApiResponse<BalanceVO> balance() {
        return ApiResponse.ok(new BalanceVO(BigDecimal.ZERO));
    }

    @GetMapping("/recharges")
    public ApiResponse<PageResult<Object>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int p = page == null ? 0 : page;
        int s = size == null ? 10 : size;
        return ApiResponse.ok(PageResult.empty(p, s));
    }

    @PostMapping("/recharges")
    public ApiResponse<Void> create(@RequestBody(required = false) Map<String, Object> body) {
        throw new BizException("钱包充值已下线");
    }

    @GetMapping("/recharges/rechargeNo/{rechargeNo}")
    public ApiResponse<Object> byNo(@PathVariable String rechargeNo) {
        return ApiResponse.ok(Collections.singletonMap("recharge", null));
    }

    @GetMapping("/recharges/{id}")
    public ApiResponse<Object> byId(@PathVariable Long id) {
        return ApiResponse.ok(Collections.singletonMap("recharge", null));
    }
}
