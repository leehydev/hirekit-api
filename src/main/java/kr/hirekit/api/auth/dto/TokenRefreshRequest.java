package kr.hirekit.api.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 갱신 요청 DTO
 * 클라이언트가 보내는 JSON을 담는 객체
 * 
 * 요청 JSON 예시:
 * {
 * "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 * }
 */
@Getter // getter 메서드 자동 생성 (getRefreshToken)
@NoArgsConstructor // 기본 생성자 자동 생성 (JSON 변환에 필요)
public class TokenRefreshRequest {

    // 클라이언트가 보낸 Refresh Token
    private String refreshToken;
}