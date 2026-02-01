package kr.hirekit.api.domain.feed.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import kr.hirekit.api.domain.answer.entity.Answer;
import kr.hirekit.api.domain.answer.entity.PassStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FeedAnswerSummary {

    private UUID id;
    private LocalDateTime createdAt;
    private UUID authorId;
    private String content;
    private String tip;
    private long likeCount;
    private PassStatus passStatus;
    private LocalDate interviewDate;
    private boolean authorHidden;

    public static FeedAnswerSummary from(Answer a, long likeCount) {
        return FeedAnswerSummary.builder()
                .id(a.getId())
                .createdAt(a.getCreatedAt())
                .authorId(a.getAuthor().getId())
                .content(a.getContent())
                .tip(a.getTip())
                .likeCount(likeCount)
                .passStatus(a.getPassStatus())
                .interviewDate(a.getInterviewDate())
                .authorHidden(a.isAuthorHidden())
                .build();
    }
}
