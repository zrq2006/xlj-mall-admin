package com.xlj.mall.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

/**
 * 无状态访问令牌：base64url(username|role|expireAt) + "." + hex(HmacSHA256)。
 */
@Component
public class TokenService {

    private final byte[] secret;
    private final long ttlMinutes;

    public TokenService(@Value("${app.auth.secret}") String secret,
                        @Value("${app.auth.token-ttl-minutes:480}") long ttlMinutes) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlMinutes = ttlMinutes;
    }

    public record Principal(String username, String role) {
    }

    public String create(String username, String role) {
        long expireAt = System.currentTimeMillis() + ttlMinutes * 60_000L;
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((username + "|" + role + "|" + expireAt).getBytes(StandardCharsets.UTF_8));
        return payload + "." + sign(payload);
    }

    public Optional<Principal> parse(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        int dot = token.lastIndexOf('.');
        if (dot <= 0) {
            return Optional.empty();
        }
        String payload = token.substring(0, dot);
        String signature = token.substring(dot + 1);
        if (!MessageDigest.isEqual(sign(payload).getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|");
            if (parts.length != 3) {
                return Optional.empty();
            }
            if (Long.parseLong(parts[2]) < System.currentTimeMillis()) {
                return Optional.empty();
            }
            return Optional.of(new Principal(parts[0], parts[1]));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16))
                        .append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("令牌签名失败", e);
        }
    }
}
