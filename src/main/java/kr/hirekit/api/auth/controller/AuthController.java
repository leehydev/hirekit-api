package kr.hirekit.api.auth.controller;

import kr.hirekit.api.auth.dto.TokenRefreshRequest;
import kr.hirekit.api.auth.dto.TokenResponse;
import kr.hirekit.api.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 인증 관련 API 컨트롤러
 * 로그인, 토큰 갱신 등 인증 관련 엔드포인트 제공
 */
@Slf4j
@RestController // REST API 컨트롤러임을 표시, JSON 반환
@RequestMapping("/api/auth") // 이 컨트롤러의 모든 API는 /api/auth로 시작
@RequiredArgsConstructor // final 필드를 주입받는 생성자 자동 생성
public class AuthController {

    // JWT 토큰 생성/검증 담당
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 토큰 갱신 API
     * Refresh Token으로 새로운 Access Token 발급
     * 
     * 요청 예시:
     * POST /api/auth/refresh
     * Body: { "refreshToken": "xxx" }
     * 
     * 응답 예시:
     * { "accessToken": "새토큰", "refreshToken": "기존토큰" }
     * 
     * @param request Refresh Token을 담은 요청 객체
     * @return 새로운 토큰 정보
     */
    @PostMapping("/refresh") // POST /api/auth/refresh 요청 처리
    public ResponseEntity<TokenResponse> refresh(@RequestBody TokenRefreshRequest request) {

        // 1. 요청에서 Refresh Token 꺼내기
        String refreshToken = request.getRefreshToken();

        // 2. Refresh Token 유효성 검증
        // - 위조된 토큰인지
        // - 만료된 토큰인지
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("토큰 갱신 실패 - 유효하지 않은 Refresh Token");

            // 401 Unauthorized 응답
            return ResponseEntity.status(401).build();
        }

        // 3. Refresh Token에서 회원 ID 추출
        UUID memberId = jwtTokenProvider.getMemberId(refreshToken);

        log.info("토큰 갱신 성공 - 회원 ID: {}", memberId);

        // 4. 새로운 Access Token 생성
        // (Refresh Token은 그대로 재사용)
        String newAccessToken = jwtTokenProvider.createAccessToken(memberId);

        // 5. 응답 객체 생성 및 반환
        TokenResponse response = new TokenResponse(newAccessToken, refreshToken);

        // 200 OK + 토큰 정보 반환
        return ResponseEntity.ok(response);
    }
}