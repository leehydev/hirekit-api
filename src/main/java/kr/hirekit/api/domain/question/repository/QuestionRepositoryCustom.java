package kr.hirekit.api.domain.question.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import kr.hirekit.api.domain.question.entity.Job;
import kr.hirekit.api.domain.question.entity.Question;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;

/**
 * 피드용 질문 목록 등 QueryDSL 기반 동적 조회.
 */
public interface QuestionRepositoryCustom {

    /**
     * 피드 노출용 질문 목록을 동적 조건으로 조회 (커서 페이지네이션).
     * <p>
     * 조건: visibility=전체공개, forcedPrivate=false.
     * 선택: companyId, job 필터. cursor(createdAt, questionId) 있으면 그 이후 구간 조회.
     * 정렬: createdAt DESC, id DESC.
     *
     * @param visibility     전체공개
     * @param companyId      회사 필터 (null이면 미적용)
     * @param job            직무 필터 (null이면 미적용)
     * @param cursorCreatedAt 커서 시각 (null이면 첫 페이지)
     * @param cursorId       커서 질문 ID (null이면 첫 페이지)
     * @param pageable       limit 등
     * @return 질문 목록
     */
    List<Question> findFeedQuestions(
            QuestionVisibility visibility,
            UUID companyId,
            Job job,
            LocalDateTime cursorCreatedAt,
            UUID cursorId,
            Pageable pageable);
}
