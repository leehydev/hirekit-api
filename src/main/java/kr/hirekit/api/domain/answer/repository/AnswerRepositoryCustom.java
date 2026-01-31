package kr.hirekit.api.domain.answer.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import kr.hirekit.api.domain.answer.entity.AnswerVisibility;

public interface AnswerRepositoryCustom {

    /**
     * 질문별 대표 답변 1개 (좋아요 많은 순, 최신순) + 좋아요 수
     * - visibility in (given list), forced_private = false
     */
    Map<UUID, RepresentativeAnswerRow> findRepresentativeAnswersByQuestionIds(
            List<UUID> questionIds,
            List<AnswerVisibility> allowedVisibilities);

    /**
     * 질문별 공개 범위별 답변 수 (forced_private = false만)
     * key: questionId, value: (visibility -> count)
     */
    Map<UUID, Map<AnswerVisibility, Long>> countByQuestionIdGroupByVisibility(List<UUID> questionIds);
}
