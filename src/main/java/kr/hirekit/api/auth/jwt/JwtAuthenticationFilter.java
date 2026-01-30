package kr.hirekit.api.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * JWT 토큰 검증 필터
 * 모든 요청에서 JWT 토큰을 확인하고 인증 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // Authorization 헤더 이름
    private static final String AUTHORIZATION_HEADER = "Authorization";

    // Bearer 토큰 접두사
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 요청에서 토큰 추출
        String token = resolveToken(request);

        // 토큰이 유효하면 인증 처리
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰에서 회원 ID 추출
            UUID memberId = jwtTokenProvider.getMemberId(token);

            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    memberId, // principal (회원 ID)
                    null, // credentials (비밀번호 없음)
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // 권한
            );

            // SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT 인증 성공 - 회원 ID: {}", memberId);
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 요청 헤더에서 토큰 추출
     * Authorization: Bearer {token}
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        // Bearer 토큰이 있으면 접두사 제거 후 반환
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}