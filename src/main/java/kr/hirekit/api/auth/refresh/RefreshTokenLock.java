package kr.hirekit.api.auth.refresh;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 토큰 리프레시 전용 분산 락 + 캐시 AOP.
 * <p>
 * 동일한 refreshToken으로 동시 요청이 오면:
 * 1) 캐시에 이미 결과가 있으면 락 없이 바로 반환
 * 2) 락을 잡은 요청만 메서드 실행(실제 갱신), 나머지는 락 대기 후 캐시에서 반환
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RefreshTokenLock {

    /**
     * Refresh Token 인자 SpEL. 예: "#refreshToken"
     */
    String key() default "#refreshToken";
}
