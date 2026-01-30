package kr.hirekit.api.auth.member.entity;

import kr.hirekit.api.common.dto.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialProvider implements CodeEnum {
    KAKAO("카카오"),
    GOOGLE("구글"),
    NAVER("네이버");

    private final String label;
}
