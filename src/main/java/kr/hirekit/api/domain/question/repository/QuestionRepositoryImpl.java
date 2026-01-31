package kr.hirekit.api.domain.question.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.dsl.BooleanExpression;
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
