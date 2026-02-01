package kr.hirekit.api.auth.service;

import kr.hirekit.api.auth.jwt.JwtTokenProvider;
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
 * Redis 분산 락 + 결과 캐시 사용 (다중 인스턴스 환경 지원).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private static final String LOCK_PREFIX = "auth:refresh:lock:";
    private static final String RESULT_PREFIX = "auth:refresh:result:";
    private static final String LOCK_VALUE_PREFIX = "lock:";
    private static final int LOCK_TTL_SECONDS = 5;
    private static final int RESULT_TTL_SECONDS = 10;
    private static final int WAIT_TIME_SECONDS = 5;
    private static final long POLL_INTERVAL_MS = 100;

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.redis.key-prefix:}")
    private String keyPrefix;

    /**
     * 동일한 refreshToken으로 동시에 여러 요청이 오면, 첫 요청만 새 Access Token을 생성하고
     * 나머지는 Redis에 캐시된 토큰을 반환합니다.
     *
     * @param refreshToken 쿠키에서 넘어온 Refresh Token (유효성 검증은 호출 전에 완료된 상태)
     * @param memberId     Refresh Token에서 추출한 회원 ID
     * @return 새 Access Token
     */
    public String refreshAccessToken(String refreshToken, UUID memberId) {
        String key = toKey(refreshToken);
        String prefix = (keyPrefix != null && !keyPrefix.isBlank()) ? keyPrefix + ":" : "";
        String resultKey = prefix + RESULT_PREFIX + key;
        String lockKey = prefix + LOCK_PREFIX + key;

        // 1. 캐시에 이미 있으면 락 없이 바로 반환
        String cached = redisTemplate.opsForValue().get(resultKey);
        if (cached != null) {
            log.debug("토큰 갱신 캐시 히트 (락 미진입)");
            return cached;
        }

        // 2. 락 획득 (대기 포함)
        String lockValue = LOCK_VALUE_PREFIX + UUID.randomUUID();
        boolean acquired = tryAcquire(lockKey, lockValue);

        if (!acquired) {
            long deadline = System.currentTimeMillis() + WAIT_TIME_SECONDS * 1000L;
            while (System.currentTimeMillis() < deadline) {
                try {
                    Thread.sleep(POLL_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("토큰 갱신 대기 중 인터럽트", e);
                }
                cached = redisTemplate.opsForValue().get(resultKey);
                if (cached != null) {
                    log.debug("토큰 갱신 캐시 대기 후 반환");
                    return cached;
                }
                acquired = tryAcquire(lockKey, lockValue);
                if (acquired) break;
            }
        }

        if (!acquired) {
            cached = redisTemplate.opsForValue().get(resultKey);
            if (cached != null) return cached;
            throw new IllegalStateException("토큰 갱신 락 획득 실패: " + lockKey);
        }

        try {
            // 3. 락 보유 중: 캐시 한 번 더 확인 후 없으면 갱신 수행
            cached = redisTemplate.opsForValue().get(resultKey);
            if (cached != null) {
                log.debug("토큰 갱신 캐시 히트 - 회원 ID: {}", memberId);
                return cached;
            }
            String newAccessToken = jwtTokenProvider.createAccessToken(memberId);
            redisTemplate.opsForValue().set(resultKey, newAccessToken, Duration.ofSeconds(RESULT_TTL_SECONDS));
            log.debug("토큰 갱신 수행 및 캐시 저장 - 회원 ID: {}", memberId);
            return newAccessToken;
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }

    private boolean tryAcquire(String lockKey, String lockValue) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofSeconds(LOCK_TTL_SECONDS)));
    }

    private void releaseLock(String lockKey, String lockValue) {
        try {
            String current = redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(current)) {
                redisTemplate.delete(lockKey);
            }
        } catch (Exception e) {
            log.warn("토큰 갱신 락 해제 중 오류 (key={}): {}", lockKey, e.getMessage());
        }
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
