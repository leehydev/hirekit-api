package kr.hirekit.api.domain.answer.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import kr.hirekit.api.domain.answer.entity.Answer;
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

    /**
     * 특정 질문의 답변 목록을 커서 기반으로 조회 (created_at DESC, id DESC).
     * - question_id = questionId, forced_private = false, visibility in allowedVisibilities
     * - cursor: (cursorCreatedAt, cursorId) 미만인 행부터 조회 (null이면 첫 페이지)
     *
     * @param questionId           질문 ID
     * @param allowedVisibilities  노출 허용 visibility 목록
     * @param cursorCreatedAt      커서 기준 시각 (null이면 무시)
     * @param cursorId             커서 기준 답변 ID (null이면 무시)
     * @param pageable             size 등 (size+1 조회 후 다음 커서 판단용으로 사용 가능)
     * @return 정렬된 답변 목록
     */
    List<Answer> findAnswersByQuestionIdCursor(
            UUID questionId,
            List<AnswerVisibility> allowedVisibilities,
            LocalDateTime cursorCreatedAt,
            UUID cursorId,
            Pageable pageable);

    /**
     * 답변 ID 목록에 대한 좋아요 수 (answer_id -> count).
     * 없는 ID는 0으로 간주하지 않고 맵에 포함하지 않음.
     */
    Map<UUID, Long> countLikesByAnswerIds(List<UUID> answerIds);
}
