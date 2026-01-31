package kr.hirekit.api.domain.answer.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import kr.hirekit.api.common.dto.BaseResponse;
import kr.hirekit.api.domain.answer.entity.Answer;
import kr.hirekit.api.domain.answer.entity.AnswerVisibility;
import kr.hirekit.api.domain.answer.entity.PassStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerDetailResponse extends BaseResponse {

    private UUID questionId;
    private String content;
    private String tip;
    private PassStatus passStatus;
    private LocalDate interviewDate;
    private boolean authorHidden;
    private AnswerVisibility visibility;

    @Builder
    public AnswerDetailResponse(UUID id, LocalDateTime createdAt, LocalDateTime updatedAt,
            UUID questionId, String content, String tip, PassStatus passStatus, LocalDate interviewDate,
            boolean authorHidden, AnswerVisibility visibility) {
        super(id, createdAt, updatedAt);
        this.questionId = questionId;
        this.content = content;
        this.tip = tip;
        this.passStatus = passStatus;
        this.interviewDate = interviewDate;
        this.authorHidden = authorHidden;
        this.visibility = visibility;
    }

    public static AnswerDetailResponse from(Answer answer) {
        return AnswerDetailResponse.builder()
                .id(answer.getId())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .questionId(answer.getQuestion().getId())
                .content(answer.getContent())
                .tip(answer.getTip())
                .passStatus(answer.getPassStatus())
                .interviewDate(answer.getInterviewDate())
                .authorHidden(answer.isAuthorHidden())
                .visibility(answer.getVisibility())
                .build();
    }
}
