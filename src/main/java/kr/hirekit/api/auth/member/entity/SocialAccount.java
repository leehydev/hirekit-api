package kr.hirekit.api.auth.member.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import kr.hirekit.api.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "social_accounts",
        // provider + providerId 조합은 유일해야 함
        uniqueConstraints = @UniqueConstraint(columnNames = { "provider", "provider_id" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount extends BaseEntity {

    // 연결된 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 소셜 로그인 제공자 (KAKAO, GOOGLE 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialProvider provider;

    // 소셜 서비스에서 제공하는 고유 ID
    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    // 연동 활성화 여부
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    // 연동 해제 일시
    @Column(name = "unlinked_at")
    private LocalDateTime unlinkedAt;

    // 빌더 패턴으로 객체 생성
    @Builder
    public SocialAccount(Member member, SocialProvider provider, String providerId) {
        this.member = member;
        this.provider = provider;
        this.providerId = providerId;
        this.isActive = true;
        this.unlinkedAt = null;
    }

    // 소셜 연동 해제
    public void unlink() {
        this.isActive = false;
        this.unlinkedAt = LocalDateTime.now();
        // providerId에 해제 정보 추가 (원본 ID 보존 + 해제 표시)
        this.providerId = "UNLINKED_" + this.unlinkedAt + "_" + this.providerId;
    }

    // 소셜 연동 재활성화 (필요 시)
    public void relink(String newProviderId) {
        this.isActive = true;
        this.unlinkedAt = null;
        this.providerId = newProviderId;
    }
}