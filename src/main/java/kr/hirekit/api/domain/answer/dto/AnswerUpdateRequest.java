package kr.hirekit.api.domain.answer.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import kr.hirekit.api.domain.answer.entity.AnswerVisibility;
import kr.hirekit.api.domain.answer.entity.PassStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AnswerUpdateRequest {

    @NotBlank(message = "답변 내용은 필수입니다.")
    private String content;

    private String tip;

    private PassStatus passStatus;

    private LocalDate interviewDate;

    @Builder.Default
    private AnswerVisibility visibility = AnswerVisibility.PUBLIC;

    @Builder.Default
    private boolean authorHidden = true;
}
