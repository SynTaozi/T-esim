package com.tesim.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties props;

    private SecretKey key() {
        byte[] bytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, 32));
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(Long memberId, String username, String role) {
        Instant now = Instant.now();
        String normalizedRole = role == null ? "USER" : role.trim().toUpperCase();
        if (normalizedRole.startsWith("ROLE_")) {
            normalizedRole = normalizedRole.substring(5);
        }
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("username", username)
                .claim("role", normalizedRole)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(props.getExpireHours(), ChronoUnit.HOURS)))
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long memberId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }
}
