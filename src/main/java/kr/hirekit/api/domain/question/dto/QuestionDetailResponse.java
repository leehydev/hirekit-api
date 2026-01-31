package kr.hirekit.api.domain.question.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import kr.hirekit.api.common.dto.BaseResponse;
import kr.hirekit.api.domain.question.entity.Job;
import kr.hirekit.api.domain.question.entity.Question;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionDetailResponse extends BaseResponse {

    private UUID companyId;
    private String companyName;
    private Job job;
    private String content;
    private boolean authorHidden;
    private QuestionVisibility visibility;

    @Builder
    public QuestionDetailResponse(UUID id, LocalDateTime createdAt, LocalDateTime updatedAt,
            UUID companyId, String companyName, Job job, String content, boolean authorHidden, QuestionVisibility visibility) {
        super(id, createdAt, updatedAt);
        this.companyId = companyId;
        this.companyName = companyName;
        this.job = job;
        this.content = content;
        this.authorHidden = authorHidden;
        this.visibility = visibility;
    }

    public static QuestionDetailResponse from(Question q) {
        return QuestionDetailResponse.builder()
                .id(q.getId())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .companyId(q.getCompany().getId())
                .companyName(q.getCompany().getName())
                .job(q.getJob())
                .content(q.getContent())
                .authorHidden(q.isAuthorHidden())
                .visibility(q.getVisibility())
                .build();
    }
}
