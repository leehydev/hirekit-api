package kr.hirekit.api.domain.answer.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import kr.hirekit.api.domain.answer.entity.Answer;
import kr.hirekit.api.domain.answer.entity.AnswerVisibility;
import kr.hirekit.api.domain.answer.entity.QAnswer;
import kr.hirekit.api.domain.answer.entity.QAnswerLike;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AnswerRepositoryImpl implements AnswerRepositoryCustom {

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Answer> searchAnswers(String keyword, List<AnswerVisibility> allowedVisibilities,
            UUID viewerMemberId, Pageable pageable) {
        QAnswer a = QAnswer.answer;

        BooleanExpression visibilityCondition = viewerMemberId != null
                ? a.visibility.in(allowedVisibilities)
                        .or(a.visibility.eq(AnswerVisibility.PRIVATE).and(a.author.id.eq(viewerMemberId)))
                : a.visibility.in(allowedVisibilities);

        List<Answer> content = queryFactory
                .selectFrom(a)
                .join(a.question).fetchJoin()
                .join(a.author).fetchJoin()
                .where(
                        a.question.visibility.eq(QuestionVisibility.PUBLIC),
                        a.question.forcedPrivate.eq(false),
                        a.forcedPrivate.eq(false),
                        contentOrTipContains(a, keyword),
                        visibilityCondition)
                .orderBy(toOrderSpecifiers(a, pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(a.count())
                .from(a)
                .where(
                        a.question.visibility.eq(QuestionVisibility.PUBLIC),
                        a.question.forcedPrivate.eq(false),
                        a.forcedPrivate.eq(false),
                        contentOrTipContains(a, keyword),
                        visibilityCondition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression contentOrTipContains(QAnswer a, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String trimmed = keyword.trim();
        return a.content.containsIgnoreCase(trimmed).or(a.tip.containsIgnoreCase(trimmed));
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(QAnswer a, Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier[] {
                    new OrderSpecifier<>(Order.DESC, a.createdAt),
                    new OrderSpecifier<>(Order.DESC, a.id)
            };
        }
        return sort.stream()
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                    return switch (order.getProperty()) {
                        case "id" -> new OrderSpecifier<>(direction, a.id);
                        case "createdAt" -> new OrderSpecifier<>(direction, a.createdAt);
                        default -> new OrderSpecifier<>(direction, a.createdAt);
                    };
                })
                .toArray(OrderSpecifier[]::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<UUID, RepresentativeAnswerRow> findRepresentativeAnswersByQuestionIds(
            List<UUID> questionIds,
            List<AnswerVisibility> allowedVisibilities) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Map.of();
        }
        String visibilityList = allowedVisibilities.stream()
                .map(v -> "'" + v.name() + "'")
                .collect(Collectors.joining(","));

        // QueryDSL JPA는 윈도우 함수(ROW_NUMBER, PARTITION BY 등)를 지원하지 않음
        // PostgreSQL: 질문별 좋아요 수 DESC, created_at DESC 로 첫 행만
        String sql = "SELECT sub.question_id, sub.answer_id, sub.like_count FROM ("
                + " SELECT a.id AS answer_id, a.question_id,"
                + "        COALESCE(l.cnt, 0)::bigint AS like_count,"
                + "        ROW_NUMBER() OVER (PARTITION BY a.question_id ORDER BY COALESCE(l.cnt, 0) DESC, a.created_at DESC) AS rn"
                + " FROM answers a"
                + " LEFT JOIN (SELECT answer_id, COUNT(*) AS cnt FROM answer_likes GROUP BY answer_id) l ON a.id = l.answer_id"
                + " WHERE a.question_id IN (:questionIds)"
                + "   AND a.forced_private = false"
                + "   AND a.visibility IN (" + visibilityList + ")"
                + ") sub WHERE sub.rn = 1";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("questionIds", questionIds);

        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> new RepresentativeAnswerRow((UUID) row[0], (UUID) row[1], ((Number) row[2]).longValue()),
                        (a, b) -> a));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<UUID, Map<AnswerVisibility, Long>> countByQuestionIdGroupByVisibility(List<UUID> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Map.of();
        }

        String sql = """
                SELECT question_id, visibility, COUNT(*) AS cnt
                FROM answers
                WHERE question_id IN (:questionIds) AND forced_private = false
                GROUP BY question_id, visibility
                """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("questionIds", questionIds);

        List<Object[]> rows = query.getResultList();
        Map<UUID, Map<AnswerVisibility, Long>> result = new java.util.HashMap<>();
        for (Object[] row : rows) {
            UUID questionId = (UUID) row[0];
            AnswerVisibility visibility = AnswerVisibility.valueOf((String) row[1]);
            long count = ((Number) row[2]).longValue();
            result.computeIfAbsent(questionId, k -> new java.util.HashMap<>()).put(visibility, count);
        }
        return result;
    }

    @Override
    public List<Answer> findAnswersByQuestionIdCursor(
            UUID questionId,
            List<AnswerVisibility> allowedVisibilities,
            UUID viewerMemberId,
            LocalDateTime cursorCreatedAt,
            UUID cursorId,
            Pageable pageable) {
        if (questionId == null || allowedVisibilities == null || allowedVisibilities.isEmpty()) {
            return List.of();
        }
        QAnswer a = QAnswer.answer;

        BooleanExpression visibilityCondition =
                viewerMemberId != null
                        ? a.visibility.in(allowedVisibilities)
                                .or(a.visibility.eq(AnswerVisibility.PRIVATE).and(a.author.id.eq(viewerMemberId)))
                        : a.visibility.in(allowedVisibilities);

        return queryFactory
                .selectFrom(a)
                .join(a.author).fetchJoin()
                .where(
                        a.question.id.eq(questionId),
                        a.forcedPrivate.eq(false),
                        visibilityCondition,
                        cursorLt(a, cursorCreatedAt, cursorId))
                .orderBy(a.createdAt.desc(), a.id.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression cursorLt(QAnswer a, LocalDateTime cursorCreatedAt, UUID cursorId) {
        if (cursorCreatedAt == null || cursorId == null) {
            return null;
        }
        return a.createdAt.lt(cursorCreatedAt)
                .or(a.createdAt.eq(cursorCreatedAt).and(a.id.lt(cursorId)));
    }

    @Override
    public Map<UUID, Long> countLikesByAnswerIds(List<UUID> answerIds) {
        if (answerIds == null || answerIds.isEmpty()) {
            return Map.of();
        }
        QAnswerLike al = QAnswerLike.answerLike;

        List<com.querydsl.core.Tuple> rows = queryFactory
                .select(al.answer.id, al.count())
                .from(al)
                .where(al.answer.id.in(answerIds))
                .groupBy(al.answer.id)
                .fetch();

        return rows.stream()
                .collect(Collectors.toMap(
                        row -> row.get(al.answer.id),
                        row -> row.get(al.count()),
                        (a, b) -> a));
    }
}
