package kr.hirekit.api.common.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * API 요청/응답 로깅 필터.
 * 요청 진입 시 메서드·URI·클라이언트 IP를, 응답 시 상태 코드·소요 시간을 로그로 남깁니다.
 */
@Slf4j
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter implements Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 로깅에서 제외할 경로 (application.yaml에서 설정 가능)
     * 기본: actuator health (헬스체크 노이즈 감소)
     */
    private final List<String> excludePaths = List.of(
            "/actuator/health",
            "/actuator/health/**"
    );

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return excludePaths.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        long startMs = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String clientIp = resolveClientIp(request);

        log.info("[API IN] {} {} {} | client={}", method, uri, query != null ? "?" + query : "", clientIp);

        try {
            filterChain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            long durationMs = System.currentTimeMillis() - startMs;
            log.info("[API OUT] {} {} | status={} | {}ms", method, uri, status, durationMs);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
