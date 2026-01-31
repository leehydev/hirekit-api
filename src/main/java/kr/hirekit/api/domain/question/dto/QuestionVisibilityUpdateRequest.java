package kr.hirekit.api.domain.question.dto;

import jakarta.validation.constraints.NotNull;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuestionVisibilityUpdateRequest {

    @NotNull(message = "공개 여부는 필수입니다.")
    private QuestionVisibility visibility;
}
