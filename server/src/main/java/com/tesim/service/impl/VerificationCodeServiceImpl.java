package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.VerificationCodeService;

import com.tesim.common.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final StringRedisTemplate redis;

    private static String key(String scene, String email) {
        return "auth:code:" + scene + ":" + email.trim().toLowerCase();
    }

    public String send(String scene, String email) {
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        redis.opsForValue().set(key(scene, email), code, Duration.ofMinutes(10));
        return code;
    }

    public void verifyOrThrow(String scene, String email, String code) {
        String cached = redis.opsForValue().get(key(scene, email));
        if (cached == null || code == null || !cached.equals(code.trim())) {
            throw new BizException("验证码错误或已过期");
        }
        redis.delete(key(scene, email));
    }
}
