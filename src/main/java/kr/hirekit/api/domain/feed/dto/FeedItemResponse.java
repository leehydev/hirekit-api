package kr.hirekit.api.domain.feed.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 피드 한 건: 질문 + 대표 답변 1개 + 권한별 답변 수.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FeedItemResponse {

    private FeedQuestionSummary question;
    /** 대표 답변 (없으면 null) */
    private FeedAnswerSummary representativeAnswer;
    /** 권한별 답변 수 → UI "로그인하고 N개 더 보기" 등 */
    private FeedAnswerCounts answerCounts;
}
