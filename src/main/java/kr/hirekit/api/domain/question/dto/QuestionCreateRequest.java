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

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuestionCreateRequest {

    @NotNull(message = "기업 ID는 필수입니다.")
    private UUID companyId;

    @NotNull(message = "직무는 필수입니다.")
    private Job job;

    @NotBlank(message = "질문 내용은 필수입니다.")
    private String content;

    @Builder.Default
    private QuestionVisibility visibility = QuestionVisibility.PUBLIC;

    @Builder.Default
    private boolean authorHidden = true;
}
