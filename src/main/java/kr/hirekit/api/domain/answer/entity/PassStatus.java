package kr.hirekit.api.domain.answer.entity;

import kr.hirekit.api.common.dto.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PassStatus implements CodeEnum {

    PASS("합격"),
    FAIL("불합격"),
    PENDING("결과 대기");

    private final String label;
}
