package kr.hirekit.api.domain.question.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.hirekit.api.domain.question.entity.Question;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    /** 첫 페이지: 전체공개·비강제비공개, 회사 필터, createdAt DESC → id DESC (커서 정렬과 동일) */
    List<Question> findByVisibilityAndForcedPrivateFalseAndCompanyIdOrderByCreatedAtDescIdDesc(
            QuestionVisibility visibility,
            UUID companyId,
            Pageable pageable);

    /** 첫 페이지: 전체공개·비강제비공개, createdAt DESC → id DESC */
    List<Question> findByVisibilityAndForcedPrivateFalseOrderByCreatedAtDescIdDesc(
            QuestionVisibility visibility,
            Pageable pageable);

    /** 다음 페이지(커서): (createdAt, id) < cursor */
    @Query("SELECT q FROM Question q WHERE q.visibility = :visibility AND q.forcedPrivate = false AND q.company.id = :companyId "
            + "AND ((q.createdAt < :cursorCreatedAt) OR (q.createdAt = :cursorCreatedAt AND q.id < :cursorId)) "
            + "ORDER BY q.createdAt DESC, q.id DESC")
    List<Question> findNextByVisibilityAndForcedPrivateFalseAndCompanyId(
            @Param("visibility") QuestionVisibility visibility,
            @Param("companyId") UUID companyId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);

    /** 다음 페이지(커서): 회사 필터 없음 */
    @Query("SELECT q FROM Question q WHERE q.visibility = :visibility AND q.forcedPrivate = false "
            + "AND ((q.createdAt < :cursorCreatedAt) OR (q.createdAt = :cursorCreatedAt AND q.id < :cursorId)) "
            + "ORDER BY q.createdAt DESC, q.id DESC")
    List<Question> findNextByVisibilityAndForcedPrivateFalse(
            @Param("visibility") QuestionVisibility visibility,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);
}
