package kr.hirekit.api.auth.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 사용자 정보 응답 DTO
 *
 * 응답 JSON 예시:
 * {
 *   "id": "550e8400-e29b-41d4-a716-446655440000",
 *   "nickname": "홍길동",
 *   "email": "kakao_123456@temp.hirekit.kr",
 *   "profileImage": "https://k.kakaocdn.net/...",
 *   "status": "PENDING",
 *   "allowedVisibilities": ["PUBLIC", "MEMBERS_ONLY"]
 * }
 */
@Getter
@AllArgsConstructor
public class UserResponse {

    /** 회원 고유 ID (UUID) */
    private String id;

    /** 닉네임 */
    private String nickname;

    /** 이메일 */
    private String email;

    /** 프로필 이미지 URL */
    private String profileImage;

    /** 회원 상태 (PENDING, ACTIVE 등) */
    private String status;

    /**
     * 이 회원이 열람할 수 있는 답변 visibility 목록 (면접 경험 1개 공유 정책 적용).
     * 프론트 상태관리·UI 분기용. ["PUBLIC"] 또는 ["PUBLIC", "MEMBERS_ONLY"].
     */
    private List<String> allowedVisibilities;
}