package kr.hirekit.api.auth.oauth2.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * OAuth2 로그인 진입 시 프론트에서 전달한 return_to(로그인 전 페이지)를 쿠키에 저장.
 * 콜백 시 Success/Failure 핸들러에서 이 쿠키를 읽어 리다이렉트 URL에 포함한다.
 */
@Slf4j
@Component
public class OAuth2ReturnToFilter extends OncePerRequestFilter {

    public static final String COOKIE_NAME = "oauth2_return_to";
    private static final int COOKIE_MAX_AGE_SECONDS = 300; // 5분

    @Value("${app.cookie-domain}")
    private String cookieDomain;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        if (!requestUri.startsWith("/oauth2/authorization/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String returnTo = request.getParameter("return_to");
        if (returnTo == null || returnTo.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!isValidReturnTo(returnTo)) {
            log.warn("OAuth2 return_to 검증 실패 (오픈 리다이렉트 방지): {}", returnTo);
            filterChain.doFilter(request, response);
            return;
        }

        String cookieValue = String.format(
                "%s=%s; Max-Age=%d; Path=/; Domain=%s; SameSite=Lax; Secure; HttpOnly",
                COOKIE_NAME, returnTo, COOKIE_MAX_AGE_SECONDS, cookieDomain
        );
        response.addHeader("Set-Cookie", cookieValue);

        filterChain.doFilter(request, response);
    }

    /**
     * return_to는 상대 경로(/)로만 허용하여 오픈 리다이렉트 방지
     */
    private boolean isValidReturnTo(String returnTo) {
        return returnTo.startsWith("/") && !returnTo.startsWith("//") && !returnTo.contains("..");
    }
}
