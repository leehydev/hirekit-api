package kr.hirekit.api.domain.answer.service;

import java.util.UUID;

import kr.hirekit.api.domain.answer.dto.AnswerCreateRequest;
import kr.hirekit.api.domain.answer.dto.AnswerDetailResponse;
import kr.hirekit.api.domain.answer.dto.AnswerLikeToggleResponse;

/**
 * 답변 등록 및 좋아요 서비스.
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
