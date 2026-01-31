package kr.hirekit.api.domain.question.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import kr.hirekit.api.domain.question.entity.Job;
import kr.hirekit.api.domain.question.entity.Question;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;
import kr.hirekit.api.domain.question.entity.QQuestion;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Question> searchQuestions(QuestionVisibility visibility, String keyword, Pageable pageable) {
        QQuestion q = QQuestion.question;

        JPAQuery<Question> query = queryFactory
                .selectFrom(q)
                .where(
                        q.visibility.eq(visibility),
                        q.forcedPrivate.eq(false),
                        contentContains(q, keyword))
                .orderBy(toOrderSpecifiers(q, pageable.getSort()));

        List<Question> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(q.count())
                .from(q)
                .where(
                        q.visibility.eq(visibility),
                        q.forcedPrivate.eq(false),
                        contentContains(q, keyword))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression contentContains(QQuestion q, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return q.content.containsIgnoreCase(keyword.trim());
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(QQuestion q, Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier[] {
                    new OrderSpecifier<>(Order.DESC, q.createdAt),
                    new OrderSpecifier<>(Order.DESC, q.id)
            };
        }
        return sort.stream()
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                    return switch (order.getProperty()) {
                        case "id" -> new OrderSpecifier<>(direction, q.id);
                        case "createdAt" -> new OrderSpecifier<>(direction, q.createdAt);
                        default -> new OrderSpecifier<>(direction, q.createdAt);
                    };
                })
                .toArray(OrderSpecifier[]::new);
    }

    @Override
    public List<Question> findFeedQuestions(
            QuestionVisibility visibility,
            UUID companyId,
            Job job,
            LocalDateTime cursorCreatedAt,
            UUID cursorId,
            Pageable pageable) {
        QQuestion q = QQuestion.question;

        return queryFactory
                .selectFrom(q)
                .where(
                        q.visibility.eq(visibility),
                        q.forcedPrivate.eq(false),
                        companyIdEq(q, companyId),
                        jobEq(q, job),
                        cursorLt(q, cursorCreatedAt, cursorId))
                .orderBy(q.createdAt.desc(), q.id.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression companyIdEq(QQuestion q, UUID companyId) {
        return companyId != null ? q.company.id.eq(companyId) : null;
    }

    private BooleanExpression jobEq(QQuestion q, Job job) {
        return job != null ? q.job.eq(job) : null;
    }

    private BooleanExpression cursorLt(QQuestion q, LocalDateTime cursorCreatedAt, UUID cursorId) {
        if (cursorCreatedAt == null || cursorId == null) {
            return null;
        }
        return q.createdAt.lt(cursorCreatedAt)
                .or(q.createdAt.eq(cursorCreatedAt).and(q.id.lt(cursorId)));
    }
}
