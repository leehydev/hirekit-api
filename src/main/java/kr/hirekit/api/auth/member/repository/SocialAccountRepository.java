package kr.hirekit.api.auth.member.repository;

import kr.hirekit.api.auth.member.entity.SocialAccount;
import kr.hirekit.api.auth.member.entity.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// 소셜 계정 레포지토리
public interface SocialAccountRepository extends JpaRepository<SocialAccount, UUID> {

    // 소셜 제공자 + 소셜 ID로 조회 (로그인 시 사용)
    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);

    // 활성화된 소셜 계정만 조회 (로그인 시 사용)
    Optional<SocialAccount> findByProviderAndProviderIdAndIsActiveTrue(SocialProvider provider, String providerId);

    // 회원의 특정 소셜 계정 조회
    Optional<SocialAccount> findByMemberIdAndProvider(UUID memberId, SocialProvider provider);

    // 회원이 해당 소셜 연동을 했는지 확인
    boolean existsByMemberIdAndProviderAndIsActiveTrue(UUID memberId, SocialProvider provider);
}