package kr.hirekit.api.domain.feed.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 질문별 답변 수 (권한별).
 * UI에서 "로그인하고 N개의 답변 더 보기" 후킹용.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FeedAnswerCounts {

    /** 해당 질문의 전체 답변 수 (강제 비공개 제외) */
    private long totalAnswerCount;

    /** 전체공개 답변 수 */
    private long publicAnswerCount;

    /** 회원만 공개 답변 수 → 비로그인 시 "로그인하고 N개 더 보기"에 사용 */
    private long membersOnlyAnswerCount;
}
