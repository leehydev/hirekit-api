package kr.hirekit.api.domain.answer.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.hirekit.api.domain.answer.entity.Answer;
import kr.hirekit.api.domain.answer.entity.AnswerVisibility;

public interface AnswerRepositoryCustom {

    /**
     * 키워드로 답변 검색 (오프셋 페이징).
     * <p>
     * 조건: 질문은 전체공개·강제비공개 아님. 답변은 forcedPrivate=false, visibility는 허용 목록에 포함되거나
     * (PRIVATE이면서 작성자=viewerMemberId). keyword가 null/blank면 content·tip 조건 없이 조회.
     * keyword가 있으면 content 또는 tip에 LIKE %keyword% 적용.
     *
     * @param keyword             검색어 (null/blank면 미적용)
     * @param allowedVisibilities 노출 허용 visibility (PUBLIC 또는 PUBLIC+MEMBERS_ONLY)
     * @param viewerMemberId      로그인 회원 ID (null이면 비로그인 → 비공개 답변 제외)
     * @param pageable            페이지, 크기, 정렬
     * @return 검색 결과 페이지
     */
    Page<Answer> searchAnswers(String keyword, List<AnswerVisibility> allowedVisibilities,
            UUID viewerMemberId, Pageable pageable);

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
     * - question_id = questionId, forced_private = false
     * - visibility: allowedVisibilities에 포함되거나, (PRIVATE 이면서 작성자 = viewerMemberId)인 경우 노출
     * - cursor: (cursorCreatedAt, cursorId) 미만인 행부터 조회 (null이면 첫 페이지)
     *
     * @param questionId           질문 ID
     * @param allowedVisibilities  노출 허용 visibility 목록 (PUBLIC 또는 PUBLIC+MEMBERS_ONLY)
     * @param viewerMemberId       로그인 회원 ID. null이 아니면 해당 회원이 작성한 비공개 답변도 포함
     * @param cursorCreatedAt      커서 기준 시각 (null이면 무시)
     * @param cursorId             커서 기준 답변 ID (null이면 무시)
     * @param pageable             size 등 (size+1 조회 후 다음 커서 판단용으로 사용 가능)
     * @return 정렬된 답변 목록
     */
    List<Answer> findAnswersByQuestionIdCursor(
            UUID questionId,
            List<AnswerVisibility> allowedVisibilities,
            UUID viewerMemberId,
            LocalDateTime cursorCreatedAt,
            UUID cursorId,
            Pageable pageable);

    /**
     * 답변 ID 목록에 대한 좋아요 수 (answer_id -> count).
     * 없는 ID는 0으로 간주하지 않고 맵에 포함하지 않음.
     */
    Map<UUID, Long> countLikesByAnswerIds(List<UUID> answerIds);
}
