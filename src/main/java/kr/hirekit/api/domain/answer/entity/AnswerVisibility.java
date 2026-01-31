package kr.hirekit.api.domain.answer.entity;

import kr.hirekit.api.common.dto.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnswerVisibility implements CodeEnum {

    PUBLIC("전체공개"),
    MEMBERS_ONLY("회원만 공개"),
    PRIVATE("비공개");

    private final String label;
}
