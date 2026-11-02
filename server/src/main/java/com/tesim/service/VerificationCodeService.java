package com.tesim.service;

import com.tesim.common.BizException;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public interface VerificationCodeService {

    String send(String scene, String email);



    void verifyOrThrow(String scene, String email, String code);


}
