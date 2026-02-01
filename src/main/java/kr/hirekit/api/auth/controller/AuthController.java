package kr.hirekit.api.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hirekit.api.auth.jwt.JwtTokenProvider;
import kr.hirekit.api.auth.service.TokenRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 인증 관련 API 컨트롤러
 * 토큰 갱신, 로그아웃 등 인증 관련 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRefreshService tokenRefreshService;

    // 쿠키 도메인 (application.yml에서 주입)
    @Value("${app.cookie-domain}")
    private String cookieDomain;

    // Access Token 만료 시간 (밀리초)
    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    // Refresh Token 만료 시간 (밀리초)
    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    /**
     * 토큰 갱신 API
     * 쿠키의 Refresh Token으로 새로운 Access Token 발급
     *
     * 요청: POST /api/auth/refresh (쿠키에 refreshToken 포함)
     * 응답: 새로운 accessToken 쿠키 설정
     */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        // 1. 쿠키에서 Refresh Token 추출
        String refreshToken = extractTokenFromCookie(request, "refreshToken");

        // 2. Refresh Token 없으면 401 반환
        if (refreshToken == null) {
            log.warn("토큰 갱신 실패 - Refresh Token 쿠키 없음");
            return ResponseEntity.status(401).build();
        }

        // 3. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("토큰 갱신 실패 - 유효하지 않은 Refresh Token");
            return ResponseEntity.status(401).build();
        }

        // 4. Refresh Token에서 회원 ID 추출
        UUID memberId = jwtTokenProvider.getMemberId(refreshToken);

        // 5. 동시 요청 시 첫 요청만 갱신, 나머지는 캐시된 토큰 반환 (Redis)
        String newAccessToken = tokenRefreshService.refreshAccessToken(refreshToken, memberId);
        log.info("토큰 갱신 성공 - 회원 ID: {}", memberId);

        // 6. 새로운 Access Token을 쿠키에 저장
        addCookieWithSameSite(response, "accessToken", newAccessToken, (int) (accessTokenExpiry / 1000));

        // 7. 204 No Content 반환 (응답 본문 없음, 쿠키만 설정)
        return ResponseEntity.noContent().build();
    }

    /**
     * 로그아웃 API
     * Access Token, Refresh Token 쿠키 삭제
     *
     * 요청: POST /api/auth/logout
     * 응답: 쿠키 삭제 (Max-Age=0)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Max-Age=0 으로 쿠키 삭제
        addCookieWithSameSite(response, "accessToken", "", 0);
        addCookieWithSameSite(response, "refreshToken", "", 0);

        log.info("로그아웃 완료");

        return ResponseEntity.noContent().build();
    }

    /**
     * 쿠키에서 특정 토큰 추출
     *
     * @param request    HTTP 요청
     * @param cookieName 쿠키 이름
     * @return 토큰 값 (없으면 null)
     */
    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        // 쿠키가 없으면 null
        if (cookies == null) {
            return null;
        }

        // 쿠키 배열에서 찾기
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     * SameSite 속성을 포함한 쿠키 추가
     *
     * @param response HTTP 응답
     * @param name     쿠키 이름
     * @param value    쿠키 값
     * @param maxAge   만료 시간 (초), 0이면 쿠키 삭제
     */
    private void addCookieWithSameSite(
            HttpServletResponse response,
            String name,
            String value,
            int maxAge) {
        String cookieValue = String.format(
                "%s=%s; Max-Age=%d; Path=/; Domain=%s; HttpOnly; Secure; SameSite=Lax",
                name,
                value,
                maxAge,
                cookieDomain);

        response.addHeader("Set-Cookie", cookieValue);
    }
}