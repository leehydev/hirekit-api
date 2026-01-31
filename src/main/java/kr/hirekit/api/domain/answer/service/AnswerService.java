package kr.hirekit.api.domain.answer.service;

import java.util.UUID;

import kr.hirekit.api.domain.answer.dto.AnswerCreateRequest;
import kr.hirekit.api.domain.answer.dto.AnswerDetailResponse;

/**
 * 답변 등록 서비스.
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
}
