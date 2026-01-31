package kr.hirekit.api.domain.answer.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.springframework.stereotype.Repository;

import kr.hirekit.api.domain.answer.entity.AnswerVisibility;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AnswerRepositoryImpl implements AnswerRepositoryCustom {

    private final EntityManager entityManager;

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
}
