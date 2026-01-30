package kr.hirekit.api.auth.oauth2;

// OAuth2 사용자 정보 인터페이스
// 카카오, 구글, 네이버 등 각 제공자별로 구현
public interface OAuth2UserInfo {

    // 소셜 서비스의 고유 ID
    String getProviderId();

    // 이메일
    String getEmail();

    // 닉네임
    String getNickname();

    // 프로필 이미지 URL
    String getProfileImage();
}
