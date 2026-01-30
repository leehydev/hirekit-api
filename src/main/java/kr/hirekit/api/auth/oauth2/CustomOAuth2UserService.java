package kr.hirekit.api.auth.oauth2;

import kr.hirekit.api.auth.member.entity.Member;
import kr.hirekit.api.auth.member.entity.SocialAccount;
import kr.hirekit.api.auth.member.entity.SocialProvider;
import kr.hirekit.api.auth.member.repository.MemberRepository;
import kr.hirekit.api.auth.member.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

// OAuth2 로그인 시 사용자 정보를 처리하는 서비스
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;

    /**
     * OAuth2 로그인 성공 시 호출됨
     * 
     * @param userRequest OAuth2UserRequest
     * @return OAuth2User
     * @throws OAuth2AuthenticationException
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 카카오에서 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 어떤 소셜 로그인인지 확인 (kakao, google 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 소셜 제공자별로 사용자 정보 파싱
        OAuth2UserInfo userInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        // 소셜 제공자 Enum 변환
        SocialProvider provider = SocialProvider.valueOf(registrationId.toUpperCase());

        // 회원 조회 또는 생성
        Member member = getOrCreateMember(provider, userInfo);

        // 인증 객체 반환 (SecurityContext에 저장됨)
        return new CustomOAuth2User(member, oAuth2User.getAttributes());
    }

    /**
     * 소셜 제공자별 사용자 정보 파싱
     * 
     * @param registrationId 소셜 제공자 ID
     * @param attributes     소셜 제공자에서 받은 원본 데이터
     * @return 사용자 정보
     */
    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return new KakaoUserInfo(attributes);
        }
        // 나중에 구글, 네이버 추가 시
        // if ("google".equals(registrationId)) {
        // return new GoogleUserInfo(attributes);
        // }
        throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인: " + registrationId);
    }

    /**
     * 회원 조회 또는 생성
     * 
     * @param provider 소셜 제공자
     * @param userInfo 사용자 정보
     * @return 회원
     */
    private Member getOrCreateMember(SocialProvider provider, OAuth2UserInfo userInfo) {

        String providerId = userInfo.getProviderId();

        // 기존 소셜 계정 조회
        Optional<SocialAccount> existingSocialAccount = socialAccountRepository
                .findByProviderAndProviderIdAndIsActiveTrue(provider, providerId);

        // 이미 가입된 소셜 계정이면 해당 회원 반환
        if (existingSocialAccount.isPresent()) {
            return existingSocialAccount.get().getMember();
        }

        // 신규 회원 생성
        Member newMember = Member.builder()
                .email(generateTempEmail(provider, providerId))
                .nickname(userInfo.getNickname() != null ? userInfo.getNickname() : "회원")
                .build();

        memberRepository.save(newMember);

        // 소셜 계정 연결
        SocialAccount socialAccount = SocialAccount.builder()
                .member(newMember)
                .provider(provider)
                .providerId(providerId)
                .build();

        socialAccountRepository.save(socialAccount);

        return newMember;
    }

    /**
     * 임시 이메일 생성 (심사 전이라 이메일 못 받음)
     * 
     * @param provider   소셜 제공자
     * @param providerId 소셜 제공자 ID
     * @return 임시 이메일
     */
    private String generateTempEmail(SocialProvider provider, String providerId) {
        return provider.name().toLowerCase() + "_" + providerId + "@noemail.local";
    }
}
