package kr.hirekit.api.domain.question.service;

import java.util.UUID;

import kr.hirekit.api.domain.answer.dto.CursorAnswerListResponse;
import kr.hirekit.api.domain.question.dto.QuestionDetailResponse;

/**
 * 질문 조회 서비스.
 */
public interface QuestionService {

    /**
     * 질문 단건 조회.
     * 전체공개(visibility=PUBLIC)이고 강제 비공개(forcedPrivate)가 아닌 질문만 조회 가능.
     *
     * @param id 질문 ID
     * @return 질문 상세
     * @throws kr.hirekit.api.common.exception.BusinessException 질문이 없거나 조회 권한이 없을 때 QUESTION_NOT_FOUND
     */
    QuestionDetailResponse getQuestion(UUID id);

    /**
     * 특정 질문의 답변 목록을 커서 기반으로 조회 (무한스크롤).
     * 질문이 전체공개가 아니거나 강제 비공개면 404. 비로그인 시 전체공개 답변만, 로그인 시 전체공개+회원공개 답변 포함.
     *
     * @param questionId 질문 ID
     * @param memberId   로그인 회원 ID (null이면 비로그인)
     * @param cursor     이전 응답의 nextCursor (첫 요청 시 null)
     * @param size       페이지 크기 (null이면 기본값, 최대 50)
     * @return 답변 목록 + nextCursor
     * @throws kr.hirekit.api.common.exception.BusinessException 질문이 없거나 조회 권한이 없을 때 QUESTION_NOT_FOUND
     */
    CursorAnswerListResponse getAnswersByQuestionId(UUID questionId, UUID memberId, String cursor, Integer size);
}
