package kr.hirekit.api.domain.question.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 특정 질문의 회원 전용(MEMBERS_ONLY) 답변 수.
 * - 회원: 0 (다 보이므로)
 * - 비회원: 회원 전용 답변 수
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MembersOnlyAnswerCountResponse {

    /** 회원 전용 답변 수 (회원이면 0, 비회원이면 해당 질문의 회원 전용 답변 수) */
    private long membersOnlyAnswerCount;

    public static MembersOnlyAnswerCountResponse of(long membersOnlyAnswerCount) {
        return MembersOnlyAnswerCountResponse.builder()
                .membersOnlyAnswerCount(membersOnlyAnswerCount)
                .build();
    }
}
