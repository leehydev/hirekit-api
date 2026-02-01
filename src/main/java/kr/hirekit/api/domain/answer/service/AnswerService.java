package kr.hirekit.api.domain.answer.service;

import java.util.UUID;

import kr.hirekit.api.common.dto.OffsetPageResponse;
import kr.hirekit.api.common.dto.PageRequest;
import kr.hirekit.api.domain.answer.dto.AnswerCreateRequest;
import kr.hirekit.api.domain.answer.dto.AnswerDetailResponse;
import kr.hirekit.api.domain.answer.dto.AnswerLikeToggleResponse;
import kr.hirekit.api.domain.answer.dto.AnswerUpdateRequest;
import kr.hirekit.api.domain.answer.dto.AnswerVisibilityUpdateRequest;

/**
 * 답변 등록·조회·수정·삭제 및 좋아요 서비스.
 */
public interface AnswerService {

    /**
     * 답변 등록.
     * 로그인한 회원이 작성자로 저장된다.
     *
     * @param questionId 질문 ID
     * @param request    등록 요청 (내용, 팁, 합격 여부, 면접 일자, 공개 여부, 작성자 숨김 여부)
     * @param memberId   작성자 회원 ID
     * @return 등록된 답변 상세
     * @throws kr.hirekit.api.common.exception.BusinessException 질문/회원이 없을 때 QUESTION_NOT_FOUND, MEMBER_NOT_FOUND
     */
    AnswerDetailResponse createAnswer(UUID questionId, AnswerCreateRequest request, UUID memberId);

    /**
     * 답변 단건 조회.
     * 질문이 전체공개이며, 답변 공개범위(visibility)가 요청자에게 허용된 경우에만 조회 가능.
     *
     * @param questionId 질문 ID
     * @param answerId   답변 ID
     * @param memberId   요청 회원 ID (비로그인 시 null → 전체공개 답변만)
     * @return 답변 상세 (작성자 ID 포함)
     * @throws kr.hirekit.api.common.exception.BusinessException QUESTION_NOT_FOUND, ANSWER_NOT_FOUND
     */
    AnswerDetailResponse getAnswer(UUID questionId, UUID answerId, UUID memberId);

    /**
     * 키워드로 답변 검색 (오프셋 페이징).
     * 전체공개 질문에 달린 답변만 대상. visibility는 "면접 경험 1개 공유" 정책 적용. keyword는 content·tip 기준 검색.
     *
     * @param keyword     검색어 (null/blank면 전체 목록)
     * @param memberId    로그인 회원 ID (null이면 비로그인 → 전체공개 답변만)
     * @param pageRequest 페이지, 크기, 정렬
     * @return 검색 결과 페이지
     */
    OffsetPageResponse<AnswerDetailResponse> searchAnswers(String keyword, UUID memberId, PageRequest pageRequest);

    /**
     * 답변 수정. 작성자만 가능.
     *
     * @param questionId 질문 ID (답변이 해당 질문에 속하는지 검증용)
     * @param answerId   답변 ID
     * @param request    수정 요청
     * @param memberId   요청 회원 ID (작성자와 일치해야 함)
     * @return 수정된 답변 상세
     * @throws kr.hirekit.api.common.exception.BusinessException QUESTION_NOT_FOUND, ANSWER_NOT_FOUND, FORBIDDEN
     */
    AnswerDetailResponse updateAnswer(UUID questionId, UUID answerId, AnswerUpdateRequest request, UUID memberId);

    /**
     * 답변 삭제. 작성자만 가능.
     *
     * @param questionId 질문 ID (답변이 해당 질문에 속하는지 검증용)
     * @param answerId   답변 ID
     * @param memberId   요청 회원 ID (작성자와 일치해야 함)
     * @throws kr.hirekit.api.common.exception.BusinessException QUESTION_NOT_FOUND, ANSWER_NOT_FOUND, FORBIDDEN
     */
    void deleteAnswer(UUID questionId, UUID answerId, UUID memberId);

    /**
     * 답변 공개상태 변경. 작성자만 가능.
     *
     * @param questionId 질문 ID (답변이 해당 질문에 속하는지 검증용)
     * @param answerId   답변 ID
     * @param request    공개상태 변경 요청
     * @param memberId   요청 회원 ID (작성자와 일치해야 함)
     * @return 변경된 답변 상세
     * @throws kr.hirekit.api.common.exception.BusinessException QUESTION_NOT_FOUND, ANSWER_NOT_FOUND, FORBIDDEN
     */
    AnswerDetailResponse updateAnswerVisibility(UUID questionId, UUID answerId,
            AnswerVisibilityUpdateRequest request, UUID memberId);

    /**
     * 답변 좋아요 토글.
     * 이미 좋아요를 눌렀으면 취소, 아니면 좋아요 추가.
     *
     * @param questionId 질문 ID (답변이 해당 질문에 속하는지 검증용)
     * @param answerId   답변 ID
     * @param memberId   요청 회원 ID
     * @return 토글 결과 (현재 좋아요 여부, 총 좋아요 수)
     * @throws kr.hirekit.api.common.exception.BusinessException 답변/회원이 없거나 답변이 해당 질문에 속하지 않을 때
     */
    AnswerLikeToggleResponse toggleLike(UUID questionId, UUID answerId, UUID memberId);
}
