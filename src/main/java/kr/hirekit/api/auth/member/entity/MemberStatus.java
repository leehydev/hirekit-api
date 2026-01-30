package kr.hirekit.api.auth.member.entity;

import kr.hirekit.api.common.dto.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 회원 상태를 나타내는 Enum
// DB에는 문자열로 저장됨 (예: "PENDING", "ACTIVE")
@Getter
@RequiredArgsConstructor
public enum MemberStatus implements CodeEnum {

    PENDING("가입대기"),
    ACTIVE("활성"),
    DORMANT("휴면"),
    BANNED("정지"),
    WITHDRAWN("탈퇴");

    private final String label;

    // name()은 Enum에 기본 내장 (PENDING, ACTIVE 등 반환)
    // getLabel()은 lombok @Getter가 자동 생성
}
