package kr.hirekit.api.auth.oauth2;

import kr.hirekit.api.auth.member.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

// Spring Security에서 사용하는 인증된 사용자 객체
// OAuth2User 인터페이스 구현 필수
@Getter
public class CustomOAuth2User implements OAuth2User {

    /**
     * 우리 DB의 회원 정보
     * 
     * @return 우리 DB의 회원 정보
     */
    private final Member member;

    /**
     * 카카오에서 받은 원본 데이터
     * 
     * @return 카카오에서 받은 원본 데이터
     */
    private final Map<String, Object> attributes;

    public CustomOAuth2User(Member member, Map<String, Object> attributes) {
        this.member = member;
        this.attributes = attributes;
    }

    /**
     * 회원 ID 조회 (자주 쓰이므로 편의 메서드)
     * 
     * @return 회원 ID
     */

    public UUID getMemberId() {
        return member.getId();
    }

    /**
     * 카카오에서 받은 원본 데이터 반환
     * 
     * @return 카카오에서 받은 원본 데이터
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * 권한 목록 반환 (ROLE_USER 기본 부여)
     * 
     * @return 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * OAuth2User 인터페이스 필수 메서드
     * 사용자 식별값 반환 (회원 ID 사용)
     * 
     * @return 사용자 식별값
     */
    @Override
    public String getName() {
        return member.getId().toString();
    }
}