package kr.hirekit.api.auth.member.entity;

import jakarta.persistence.*;
import kr.hirekit.api.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    // 이메일
    @Column(nullable = false, length = 255)
    private String email;

    // 닉네임
    @Column(nullable = false, length = 50)
    private String nickname;

    // 프로필 이미지 URL
    @Column(name = "profile_image", length = 500)
    private String profileImage;

    // 회원 상태 (PENDING, ACTIVE 등)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MemberStatus status;

    // 빌더 패턴으로 객체 생성
    @Builder
    public Member(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
        this.status = MemberStatus.PENDING;
    }

    // 회원 상태 변경
    public void updateStatus(MemberStatus status) {
        this.status = status;
    }

    // 프로필 정보 수정
    public void updateProfile(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
    }
}
