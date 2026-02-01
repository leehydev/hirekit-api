package kr.hirekit.api.domain.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.hirekit.api.domain.question.entity.Job;
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
public class QuestionUpdateRequest {

    @NotNull(message = "직무는 필수입니다.")
    private Job job;

    @NotBlank(message = "질문 내용은 필수입니다.")
    private String content;

    @NotNull(message = "공개 여부는 필수입니다.")
    private QuestionVisibility visibility;

    @Builder.Default
    private boolean authorHidden = true;
}
