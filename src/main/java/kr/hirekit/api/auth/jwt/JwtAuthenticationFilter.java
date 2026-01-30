package kr.hirekit.api.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * JWT 토큰 검증 필터
 * 모든 요청에서 쿠키의 JWT 토큰을 확인하고 인증 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // 쿠키 이름 상수
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. 쿠키에서 Access Token 추출
        String token = resolveTokenFromCookie(request);

        // 2. 토큰이 유효하면 인증 처리
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // 토큰에서 회원 ID 추출
            UUID memberId = jwtTokenProvider.getMemberId(token);

            // 인증 객체 생성
            // - principal: 회원 ID (누구인지)
            // - credentials: null (비밀번호 없음)
            // - authorities: 권한 목록 (ROLE_USER)
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

            // SecurityContext에 인증 정보 저장
            // → 이후 컨트롤러에서 인증된 사용자 정보 사용 가능
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT 인증 성공 - 회원 ID: {}", memberId);
        }

        // 3. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 쿠키에서 Access Token 추출
     *
     * @param request HTTP 요청
     * @return Access Token 문자열 (없으면 null)
     */
    private String resolveTokenFromCookie(HttpServletRequest request) {
        // 요청에 쿠키가 없으면 null 반환
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        // 쿠키 배열에서 accessToken 찾기
        for (Cookie cookie : cookies) {
            if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        // accessToken 쿠키가 없으면 null 반환
        return null;
    }
}