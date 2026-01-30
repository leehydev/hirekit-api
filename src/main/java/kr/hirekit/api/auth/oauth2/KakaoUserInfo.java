package kr.hirekit.api.auth.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

// 카카오 사용자 정보
// 심사 없이 받을 수 있는 정보만 사용
public class KakaoUserInfo implements OAuth2UserInfo {

    // 카카오 고유 ID
    private final String id;

    // 닉네임
    private final String nickname;

    // 프로필 이미지 URL
    private final String profileImage;

    public KakaoUserInfo(Map<String, Object> attributes) {
        ObjectMapper mapper = new ObjectMapper();

        // 카카오 고유 ID (필수, 항상 제공됨)
        this.id = String.valueOf(attributes.get("id"));

        // kakao_account -> profile 안전하게 파싱
        String tempNickname = null;
        String tempProfileImage = null;

        Object kakaoAccountObj = attributes.get("kakao_account");
        if (kakaoAccountObj != null) {
            // Object -> KakaoAccount 클래스로 변환
            KakaoAccount kakaoAccount = mapper.convertValue(kakaoAccountObj, KakaoAccount.class);
            if (kakaoAccount.profile() != null) {
                tempNickname = kakaoAccount.profile().nickname();
                tempProfileImage = kakaoAccount.profile().profileImageUrl();
            }
        }

        this.nickname = tempNickname;
        this.profileImage = tempProfileImage;
    }

    @Override
    public String getProviderId() {
        return id;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public String getProfileImage() {
        return profileImage;
    }

    // 내부 파싱용 record (Java 17)
    private record KakaoAccount(KakaoProfile profile) {
    }

    private record KakaoProfile(
            String nickname,
            String profileImageUrl) {
    }
}