package kr.hirekit.api.domain.question.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hirekit.api.auth.member.entity.Member;
import kr.hirekit.api.auth.member.repository.MemberRepository;
import kr.hirekit.api.common.dto.ErrorCode;
import kr.hirekit.api.common.exception.BusinessException;
import kr.hirekit.api.domain.answer.dto.AnswerCursor;
import kr.hirekit.api.domain.answer.dto.AnswerListItemResponse;
import kr.hirekit.api.domain.answer.dto.CursorAnswerListResponse;
import kr.hirekit.api.domain.answer.entity.Answer;
import kr.hirekit.api.domain.answer.entity.AnswerVisibility;
import kr.hirekit.api.domain.answer.repository.AnswerRepository;
import kr.hirekit.api.domain.company.entity.Company;
import kr.hirekit.api.domain.company.repository.CompanyRepository;
import kr.hirekit.api.domain.question.dto.QuestionCreateRequest;
import kr.hirekit.api.domain.question.dto.QuestionDetailResponse;
import kr.hirekit.api.domain.question.entity.Question;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;
import kr.hirekit.api.domain.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionServiceImpl implements QuestionService {

    private static final int DEFAULT_ANSWER_PAGE_SIZE = 20;
    private static final int MAX_ANSWER_PAGE_SIZE = 50;

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public QuestionDetailResponse createQuestion(QuestionCreateRequest request, UUID memberId) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Question question = questionRepository.save(Question.builder()
                .company(company)
                .job(request.getJob())
                .content(request.getContent())
                .author(author)
                .visibility(request.getVisibility())
                .authorHidden(request.isAuthorHidden())
                .forcedPrivate(false)
                .build());
        return QuestionDetailResponse.from(question);
    }

    @Override
    public QuestionDetailResponse getQuestion(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        if (question.getVisibility() != QuestionVisibility.PUBLIC || question.isForcedPrivate()) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }

        return QuestionDetailResponse.from(question);
    }

    @Override
    public CursorAnswerListResponse getAnswersByQuestionId(UUID questionId, UUID memberId, String cursor, Integer size) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
        if (question.getVisibility() != QuestionVisibility.PUBLIC || question.isForcedPrivate()) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }

        int pageSize = size != null ? Math.min(Math.max(1, size), MAX_ANSWER_PAGE_SIZE) : DEFAULT_ANSWER_PAGE_SIZE;
        List<AnswerVisibility> allowedVisibilities = memberId != null
                ? List.of(AnswerVisibility.PUBLIC, AnswerVisibility.MEMBERS_ONLY)
                : List.of(AnswerVisibility.PUBLIC);

        Optional<AnswerCursor> parsed = AnswerCursor.parse(cursor);
        var cursorCreatedAt = parsed.map(AnswerCursor::getCreatedAt).orElse(null);
        var cursorId = parsed.map(AnswerCursor::getAnswerId).orElse(null);

        List<Answer> answers = answerRepository.findAnswersByQuestionIdCursor(
                questionId, allowedVisibilities, cursorCreatedAt, cursorId, PageRequest.of(0, pageSize));

        if (answers.isEmpty()) {
            return CursorAnswerListResponse.builder().items(List.of()).nextCursor(null).build();
        }

        List<UUID> answerIds = answers.stream().map(Answer::getId).toList();
        var likeCounts = answerRepository.countLikesByAnswerIds(answerIds);

        List<AnswerListItemResponse> items = answers.stream()
                .map(a -> AnswerListItemResponse.from(a, likeCounts.getOrDefault(a.getId(), 0L)))
                .toList();

        Answer last = answers.get(answers.size() - 1);
        String nextCursor = answers.size() == pageSize
                ? AnswerCursor.encode(last.getCreatedAt(), last.getId())
                : null;

        return CursorAnswerListResponse.builder()
                .items(items)
                .nextCursor(nextCursor)
                .build();
    }
}
