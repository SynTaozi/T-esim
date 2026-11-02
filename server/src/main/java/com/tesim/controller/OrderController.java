package com.tesim.controller;

import com.tesim.common.ApiResponse;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.order.CreateOrderRequest;
import com.tesim.dto.order.GuestOrderLookupRequest;
import com.tesim.dto.order.OrderVO;
import com.tesim.service.OrderAppService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final int LOOKUP_LIMIT = 10;
    private static final Duration LOOKUP_WINDOW = Duration.ofMinutes(15);

    private final OrderAppService orderAppService;
    private final StringRedisTemplate redis;

    @PostMapping
    public ApiResponse<OrderVO> create(@RequestBody CreateOrderRequest body) {
        return ApiResponse.ok(orderAppService.create(body));
    }

    @PostMapping("/guest-lookup")
    public ApiResponse<List<OrderVO>> guestLookup(@RequestBody GuestOrderLookupRequest body, HttpServletRequest request) {
        String ip = clientIp(request);
        String emailKey = StringUtils.hasText(body.getEmail()) ? sha256(body.getEmail().trim().toLowerCase()) : "none";
        assertLookupAllowed("guest_lookup:ip:" + ip);
        assertLookupAllowed("guest_lookup:email:" + emailKey);
        try {
            List<OrderVO> orders = orderAppService.guestLookup(body.getEmail());
            // Count empty results against rate limit to reduce email probing.
            if (orders == null || orders.isEmpty()) {
                bumpLookup(ip, emailKey);
            }
            return ApiResponse.ok(orders);
        } catch (BizException e) {
            bumpLookup(ip, emailKey);
            throw e;
        }
    }

    @GetMapping
    public ApiResponse<List<OrderVO>> list() {
        return ApiResponse.ok(orderAppService.listMine());
    }

    @GetMapping("/paginated")
    public ApiResponse<PageResult<OrderVO>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ApiResponse.ok(orderAppService.paginated(page, size));
    }

    @GetMapping("/my-orders")
    public ApiResponse<PageResult<OrderVO>> myOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(orderAppService.myOrders(page, pageSize, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderAppService.getById(id));
    }

    @GetMapping("/no/{orderNo}")
    public ApiResponse<OrderVO> byNo(@PathVariable String orderNo) {
        return ApiResponse.ok(orderAppService.getByNo(orderNo));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<OrderVO> cancel(@PathVariable Long id) {
        return ApiResponse.ok(orderAppService.cancel(id));
    }

    @PutMapping("/{id}/pay")
    public ApiResponse<OrderVO> pay(@PathVariable Long id) {
        return ApiResponse.ok(orderAppService.pay(id));
    }

    private void assertLookupAllowed(String key) {
        String raw = redis.opsForValue().get(key);
        if (raw != null) {
            try {
                if (Integer.parseInt(raw) >= LOOKUP_LIMIT) {
                    throw new BizException(429, "查询过于频繁，请稍后再试");
                }
            } catch (NumberFormatException ignored) {
                // reset below on bump
            }
        }
    }

    private void bumpLookup(String ip, String emailKey) {
        bump("guest_lookup:ip:" + ip);
        bump("guest_lookup:email:" + emailKey);
    }

    private void bump(String key) {
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, LOOKUP_WINDOW);
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String real = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(real)) {
            return real.trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return Integer.toHexString(value.hashCode());
        }
    }
}
