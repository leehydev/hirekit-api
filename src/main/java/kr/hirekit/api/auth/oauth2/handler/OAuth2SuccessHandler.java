package kr.hirekit.api.auth.oauth2.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hirekit.api.auth.jwt.JwtTokenProvider;
import kr.hirekit.api.auth.oauth2.CustomOAuth2User;
import kr.hirekit.api.auth.oauth2.filter.OAuth2ReturnToFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

/**
 * OAuth2 로그인 성공 시 처리하는 핸들러
 * JWT 토큰을 HttpOnly 쿠키에 저장 후 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.cookie-domain}")
    private String cookieDomain;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // 1. 인증된 사용자 정보 가져오기
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        UUID memberId = oAuth2User.getMemberId();

        log.info("OAuth2 로그인 성공 - 회원 ID: {}", memberId);

        // 2. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(memberId);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId);

        // 3. 쿠키에 토큰 저장 (SameSite=Lax 포함)
        addCookieWithSameSite(response, "accessToken", accessToken, (int) (accessTokenExpiry / 1000));
        addCookieWithSameSite(response, "refreshToken", refreshToken, (int) (refreshTokenExpiry / 1000));

        // 4. return_to 쿠키가 있으면 리다이렉트 URL에 포함 후 쿠키 삭제
        String redirectUrl = buildCallbackRedirectUrl(request, response);

        // 5. 프론트엔드로 리다이렉트
        response.sendRedirect(redirectUrl);
    }

    private String buildCallbackRedirectUrl(HttpServletRequest request, HttpServletResponse response) {
        String base = frontendUrl + "/oauth/callback";
        String returnTo = getReturnToFromCookie(request);
        if (returnTo == null || returnTo.isBlank()) {
            return base;
        }
        clearReturnToCookie(response);
        return base + "?return_to=" + URLEncoder.encode(returnTo, StandardCharsets.UTF_8);
    }

    private String getReturnToFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> OAuth2ReturnToFilter.COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void clearReturnToCookie(HttpServletResponse response) {
        String cookieValue = String.format(
                "%s=; Max-Age=0; Path=/; Domain=%s; SameSite=Lax; Secure; HttpOnly",
                OAuth2ReturnToFilter.COOKIE_NAME, cookieDomain
        );
        response.addHeader("Set-Cookie", cookieValue);
    }

    /**
     * SameSite 속성을 포함한 쿠키 추가
     * Java Cookie 클래스가 SameSite를 지원 안 해서 헤더로 직접 작성
     *
     * @param response HTTP 응답 객체
     * @param name     쿠키 이름
     * @param value    쿠키 값 (토큰)
     * @param maxAge   만료 시간 (초)
     */
    private void addCookieWithSameSite(
            HttpServletResponse response,
            String name,
            String value,
            int maxAge) {
        // Set-Cookie 헤더 형식으로 직접 작성
        // 예: accessToken=xxx; Max-Age=1800; Path=/; Domain=.hirekit-dev.kr; HttpOnly;
        // Secure; SameSite=Lax
        String cookieValue = String.format(
                "%s=%s; Max-Age=%d; Path=/; Domain=%s; HttpOnly; Secure; SameSite=Lax",
                name, // 쿠키 이름
                value, // 쿠키 값
                maxAge, // 만료 시간 (초)
                cookieDomain // 도메인 (.hirekit-dev.kr)
        );

        // 응답 헤더에 추가
        response.addHeader("Set-Cookie", cookieValue);
    }
}