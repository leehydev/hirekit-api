package kr.hirekit.api.domain.answer.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 답변 좋아요 토글 API 응답.
 * liked: 현재 요청 회원이 해당 답변에 좋아요를 눌렀는지 여부
 * likeCount: 해당 답변의 총 좋아요 수
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AnswerLikeToggleResponse {

    private boolean liked;
    private long likeCount;
}
