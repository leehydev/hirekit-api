package kr.hirekit.api.domain.feed.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import kr.hirekit.api.domain.question.entity.Job;
import kr.hirekit.api.domain.question.entity.Question;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FeedQuestionSummary {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UUID companyId;
    private String companyName;
    private Job job;
    private String content;
    private boolean authorHidden;

    public static FeedQuestionSummary from(Question q) {
        return FeedQuestionSummary.builder()
                .id(q.getId())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .companyId(q.getCompany().getId())
                .companyName(q.getCompany().getName())
                .job(q.getJob())
                .content(q.getContent())
                .authorHidden(q.isAuthorHidden())
                .build();
    }
}
