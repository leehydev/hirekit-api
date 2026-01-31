package kr.hirekit.api.auth.service;

import kr.hirekit.api.auth.jwt.JwtTokenProvider;
import kr.hirekit.api.auth.refresh.RefreshTokenLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.UUID;

/**
 * 토큰 리프레시 시 동시 요청을 하나로 묶어, 첫 요청만 실제 갱신하고 나머지는 캐시된 Access Token을 반환합니다.
 * {@link RefreshTokenLock} 전용 AOP + Redis 결과 캐시 사용 (다중 인스턴스 환경 지원).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private static final String RESULT_PREFIX = "auth:refresh:result:";
    private static final int RESULT_TTL_SECONDS = 10;

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.redis.key-prefix:}")
    private String keyPrefix;

    /**
     * 동일한 refreshToken으로 동시에 여러 요청이 오면, 첫 요청만 새 Access Token을 생성하고
     * 나머지는 Redis에 캐시된 토큰을 반환합니다.
     * 락·캐시 선조회·대기는 {@link RefreshTokenLock} 전용 AOP에서 처리됩니다.
     *
     * @param refreshToken 쿠키에서 넘어온 Refresh Token (유효성 검증은 호출 전에 완료된 상태)
     * @param memberId     Refresh Token에서 추출한 회원 ID
     * @return 새 Access Token
     */
    @RefreshTokenLock
    public String refreshAccessToken(String refreshToken, UUID memberId) {
        String resultKey = buildResultKey(refreshToken);

        // 1. 이미 캐시된 결과가 있으면 바로 반환
        String cached = redisTemplate.opsForValue().get(resultKey);
        if (cached != null) {
            log.debug("토큰 갱신 캐시 히트 - 회원 ID: {}", memberId);
            return cached;
        }

        // 2. 락을 잡은 요청만 여기 도달. 새 Access Token 생성 후 캐시에 저장
        String newAccessToken = jwtTokenProvider.createAccessToken(memberId);
        redisTemplate.opsForValue().set(resultKey, newAccessToken, Duration.ofSeconds(RESULT_TTL_SECONDS));
        log.debug("토큰 갱신 수행 및 캐시 저장 - 회원 ID: {}", memberId);
        return newAccessToken;
    }

    private String buildResultKey(String refreshToken) {
        String prefix = (keyPrefix != null && !keyPrefix.isBlank()) ? keyPrefix + ":" : "";
        return prefix + RESULT_PREFIX + toKey(refreshToken);
    }

    /**
     * Refresh Token 문자열을 Redis 키(락·캐시)용 해시로 변환.
     */
    public static String toKey(String refreshToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, Math.min(32, hex.length()));
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(refreshToken.hashCode());
        }
    }
}

