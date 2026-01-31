package kr.hirekit.api.domain.question.service;

import java.util.UUID;

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
}
