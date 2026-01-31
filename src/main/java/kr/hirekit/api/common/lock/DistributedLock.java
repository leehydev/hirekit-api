package kr.hirekit.api.common.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 실행 시 Redis 분산 락을 걸 때 사용하는 어노테이션.
 * <p>
 * 락 키는 SpEL로 지정하며, 메서드 인자명을 사용할 수 있다.
 * 예: {@code @DistributedLock(key = "#refreshToken")}
 * 예: {@code @DistributedLock(key = "'order:' + #orderId", prefix = "lock")}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락 키 (SpEL). 메서드 파라미터 참조 예: #paramName
     */
    String key();

    /**
     * 락 키 앞에 붙을 prefix. 비우면 app.redis.key-prefix 만 적용.
     */
    String prefix() default "lock";

    /**
     * 락을 못 잡았을 때 대기 시간(초). 0이면 대기 없이 한 번만 시도 후 실패 시 예외.
     */
    long waitTimeSeconds() default 0;

    /**
     * 락 유지 시간(초). Redis TTL. 이 시간이 지나면 락이 자동 해제된다.
     */
    long leaseTimeSeconds() default 10;
}
