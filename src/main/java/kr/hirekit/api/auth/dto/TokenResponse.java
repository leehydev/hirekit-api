package kr.hirekit.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 토큰 응답 DTO
 * 서버가 클라이언트에게 보내는 JSON을 담는 객체
 * 
 * 응답 JSON 예시:
 * {
 * "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 * "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 * }
 */
@Getter // getter 메서드 자동 생성
@AllArgsConstructor // 모든 필드를 받는 생성자 자동 생성
public class TokenResponse {

    // API 요청 시 사용하는 토큰 (30분 유효)
    private String accessToken;

    // Access Token 갱신용 토큰 (7일 유효)
    private String refreshToken;
}