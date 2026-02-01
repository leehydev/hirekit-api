package kr.hirekit.api.auth.member.repository;

import kr.hirekit.api.auth.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// 회원 레포지토리
public interface MemberRepository extends JpaRepository<Member, UUID> {

    // 이메일로 회원 조회
    Optional<Member> findByEmail(String email);

    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
}