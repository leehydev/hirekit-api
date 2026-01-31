package kr.hirekit.api.common.lock;

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
 * {@link DistributedLock} 어노테이션이 붙은 메서드 실행 시 Redis 분산 락을 걸고 해제한다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private static final String LOCK_VALUE_PREFIX = "lock:";
    private static final long POLL_INTERVAL_MS = 100;

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.redis.key-prefix:}")
    private String keyPrefix;

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = resolveLockKey(joinPoint, distributedLock);
        String lockValue = LOCK_VALUE_PREFIX + UUID.randomUUID();
        long leaseTimeSeconds = distributedLock.leaseTimeSeconds();
        long waitTimeSeconds = distributedLock.waitTimeSeconds();

        boolean acquired = tryAcquire(lockKey, lockValue, leaseTimeSeconds);

        if (!acquired && waitTimeSeconds > 0) {
            long deadline = System.currentTimeMillis() + waitTimeSeconds * 1000;
            while (System.currentTimeMillis() < deadline) {
                Thread.sleep(POLL_INTERVAL_MS);
                acquired = tryAcquire(lockKey, lockValue, leaseTimeSeconds);
                if (acquired) break;
            }
        }

        if (!acquired) {
            throw new LockAcquisitionException("락 획득 실패: " + lockKey);
        }

        try {
            return joinPoint.proceed();
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }

    private String resolveLockKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        Object keyObj = parser.parseExpression(distributedLock.key()).getValue(context);
        String keyPart = keyObj != null ? keyObj.toString() : "";
        String prefix = (keyPrefix != null && !keyPrefix.isBlank()) ? keyPrefix + ":" : "";
        String lockPrefix = (distributedLock.prefix() != null && !distributedLock.prefix().isBlank())
                ? distributedLock.prefix() + ":"
                : "";
        return prefix + lockPrefix + keyPart;
    }

    private boolean tryAcquire(String lockKey, String lockValue, long leaseTimeSeconds) {
        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(leaseTimeSeconds));
        return Boolean.TRUE.equals(ok);
    }

    private void releaseLock(String lockKey, String lockValue) {
        try {
            String current = redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(current)) {
                redisTemplate.delete(lockKey);
            }
        } catch (Exception e) {
            log.warn("락 해제 중 오류 (key={}): {}", lockKey, e.getMessage());
        }
    }
}
