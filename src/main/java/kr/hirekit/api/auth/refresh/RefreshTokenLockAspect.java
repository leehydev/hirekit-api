package kr.hirekit.api.auth.refresh;

import kr.hirekit.api.auth.service.TokenRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * {@link RefreshTokenLock} 전용 AOP.
 * 캐시 선조회 → 락 획득(대기) → 메서드 실행 → 락 해제.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RefreshTokenLockAspect {

    private static final String LOCK_PREFIX = "auth:refresh:lock:";
    private static final String RESULT_PREFIX = "auth:refresh:result:";
    private static final String LOCK_VALUE_PREFIX = "lock:";
    private static final int LOCK_TTL_SECONDS = 5;
    private static final int WAIT_TIME_SECONDS = 5;
    private static final long POLL_INTERVAL_MS = 100;

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.redis.key-prefix:}")
    private String keyPrefix;

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(refreshTokenLock)")
    public Object around(ProceedingJoinPoint joinPoint, RefreshTokenLock refreshTokenLock) throws Throwable {
        String refreshToken = resolveRefreshToken(joinPoint, refreshTokenLock);
        String key = TokenRefreshService.toKey(refreshToken);
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
                Thread.sleep(POLL_INTERVAL_MS);
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
            // 락 대기 타임아웃 시에도 캐시 한 번 더 확인
            cached = redisTemplate.opsForValue().get(resultKey);
            if (cached != null) return cached;
            throw new IllegalStateException("토큰 갱신 락 획득 실패: " + lockKey);
        }

        try {
            return joinPoint.proceed();
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }

    private String resolveRefreshToken(ProceedingJoinPoint joinPoint, RefreshTokenLock refreshTokenLock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        Object value = parser.parseExpression(refreshTokenLock.key()).getValue(context);
        return value != null ? value.toString() : "";
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
}
