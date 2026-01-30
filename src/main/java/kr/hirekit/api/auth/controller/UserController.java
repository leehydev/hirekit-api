package kr.hirekit.api.auth.controller;

import kr.hirekit.api.auth.dto.UserResponse;
import kr.hirekit.api.auth.member.entity.Member;
import kr.hirekit.api.auth.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 사용자 정보 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final MemberRepository memberRepository;

    /**
     * 현재 로그인한 사용자 정보 조회
     *
     * 요청: GET /api/users/me
     * 응답: { id, nickname, email, profileImage, status }
     *
     * @param memberId JWT에서 추출한 회원 ID (Spring Security가 자동 주입)
     * @return 사용자 정보
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UUID memberId) {
        log.info("사용자 정보 조회 - 회원 ID: {}", memberId);

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElse(null);

        // 회원이 없으면 404 반환
        if (member == null) {
            log.warn("회원을 찾을 수 없음 - ID: {}", memberId);
            return ResponseEntity.notFound().build();
        }

        // 응답 객체 생성
        UserResponse response = new UserResponse(
                member.getId().toString(),
                member.getNickname(),
                member.getEmail(),
                member.getProfileImage(),
                member.getStatus().name());

        return ResponseEntity.ok(response);
    }
}