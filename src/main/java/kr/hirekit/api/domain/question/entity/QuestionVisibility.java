package kr.hirekit.api.domain.question.entity;

import kr.hirekit.api.common.dto.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionVisibility implements CodeEnum {

    PUBLIC("전체공개"),
    PRIVATE("비공개");

    private final String label;
}
