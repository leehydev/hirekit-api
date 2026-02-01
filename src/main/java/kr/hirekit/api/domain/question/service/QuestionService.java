package kr.hirekit.api.domain.question.service;

import java.util.UUID;

import kr.hirekit.api.common.dto.CursorPageResponse;
import kr.hirekit.api.common.dto.OffsetPageResponse;
import kr.hirekit.api.common.dto.PageRequest;
import kr.hirekit.api.domain.answer.dto.AnswerListItemResponse;
import kr.hirekit.api.domain.question.dto.MembersOnlyAnswerCountResponse;
import kr.hirekit.api.domain.question.dto.QuestionCreateRequest;
import kr.hirekit.api.domain.question.dto.QuestionDetailResponse;
import kr.hirekit.api.domain.question.dto.QuestionUpdateRequest;
import kr.hirekit.api.domain.question.dto.QuestionVisibilityUpdateRequest;

/**
 * 질문 조회 및 등록 서비스.
 */
public interface QuestionService {

    /**
     * 질문 등록.
     * 로그인한 회원이 작성자로 저장된다.
     *
     * @param request  등록 요청 (기업 ID, 직무, 내용, 공개 여부, 작성자 숨김 여부)
     * @param memberId 작성자 회원 ID
     * @return 등록된 질문 상세
     * @throws kr.hirekit.api.common.exception.BusinessException 기업/회원이 없을 때 COMPANY_NOT_FOUND, MEMBER_NOT_FOUND
     */
    QuestionDetailResponse createQuestion(QuestionCreateRequest request, UUID memberId);

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
     * 키워드로 질문 검색 (오프셋 페이징).
     * 전체공개(visibility=PUBLIC), 강제 비공개가 아닌 질문만 대상. keyword는 content 기준 LIKE 검색.
     *
     * @param keyword     검색어 (null/blank면 전체 공개 질문 목록)
     * @param pageRequest 페이지, 크기, 정렬
     * @return 검색 결과 페이지 (목록 + totalElements, totalPages 등)
     */
    OffsetPageResponse<QuestionDetailResponse> searchQuestions(String keyword, PageRequest pageRequest);

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
    CursorPageResponse<AnswerListItemResponse> getAnswersByQuestionId(UUID questionId, UUID memberId, String cursor, Integer size);

    /**
     * 특정 질문의 회원 전용 답변 수 조회 (public API).
     * 회원이면 0, 비회원이면 해당 질문의 회원 전용(MEMBERS_ONLY) 답변 수를 반환.
     *
     * @param questionId 질문 ID
     * @param memberId   로그인 회원 ID (null이면 비회원)
     * @return membersOnlyAnswerCount (회원 0, 비회원 회원 전용 답변 수)
     * @throws kr.hirekit.api.common.exception.BusinessException 질문이 없거나 조회 권한이 없을 때 QUESTION_NOT_FOUND
     */
    MembersOnlyAnswerCountResponse getMembersOnlyAnswerCount(UUID questionId, UUID memberId);

    /**
     * 질문 수정. 작성자만 가능하며, 답변이 하나도 없을 때만 가능.
     *
     * @param id        질문 ID
     * @param request   수정 요청 (직무, 내용, 공개 여부, 작성자 숨김 여부)
     * @param memberId  요청 회원 ID (작성자와 일치해야 함)
     * @return 수정된 질문 상세
     * @throws kr.hirekit.api.common.exception.BusinessException QUESTION_NOT_FOUND, FORBIDDEN, QUESTION_HAS_ANSWERS
     */
    QuestionDetailResponse updateQuestion(UUID id, QuestionUpdateRequest request, UUID memberId);

    /**
     * 질문 삭제. 작성자만 가능하며, 답변이 하나도 없을 때만 가능.
     *
     * @param id       질문 ID
     * @param memberId 요청 회원 ID (작성자와 일치해야 함)
     * @throws kr.hirekit.api.common.exception.BusinessException QUESTION_NOT_FOUND, FORBIDDEN, QUESTION_HAS_ANSWERS
     */
    void deleteQuestion(UUID id, UUID memberId);

    /**
     * 질문 공개상태 변경. 작성자만 가능하며, 답변이 하나도 없을 때만 가능.
     *
     * @param id        질문 ID
     * @param request   공개상태 변경 요청
     * @param memberId  요청 회원 ID (작성자와 일치해야 함)
     * @return 변경된 질문 상세
     * @throws kr.hirekit.api.common.exception.BusinessException QUESTION_NOT_FOUND, FORBIDDEN, QUESTION_HAS_ANSWERS
     */
    QuestionDetailResponse updateQuestionVisibility(UUID id, QuestionVisibilityUpdateRequest request, UUID memberId);
}
