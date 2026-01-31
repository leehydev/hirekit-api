package kr.hirekit.api.domain.answer.dto;

import jakarta.validation.constraints.NotNull;
import kr.hirekit.api.domain.answer.entity.AnswerVisibility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AnswerVisibilityUpdateRequest {

    @NotNull(message = "공개 여부는 필수입니다.")
    private AnswerVisibility visibility;
}
