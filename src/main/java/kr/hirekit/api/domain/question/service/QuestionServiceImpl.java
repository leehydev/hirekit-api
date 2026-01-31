package kr.hirekit.api.domain.question.service;

import java.util.List;
import java.util.Map;
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
import kr.hirekit.api.common.dto.CursorPageResponse;
import kr.hirekit.api.domain.answer.entity.Answer;
import kr.hirekit.api.domain.answer.entity.AnswerVisibility;
import kr.hirekit.api.domain.answer.repository.AnswerRepository;
import kr.hirekit.api.domain.answer.service.MemberAnswerAccessService;
import kr.hirekit.api.domain.company.entity.Company;
import kr.hirekit.api.domain.company.repository.CompanyRepository;
import kr.hirekit.api.domain.question.dto.MembersOnlyAnswerCountResponse;
import kr.hirekit.api.domain.question.dto.QuestionCreateRequest;
import kr.hirekit.api.domain.question.dto.QuestionDetailResponse;
import kr.hirekit.api.domain.question.dto.QuestionUpdateRequest;
import kr.hirekit.api.domain.question.dto.QuestionVisibilityUpdateRequest;
import kr.hirekit.api.domain.question.entity.Question;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;
import kr.hirekit.api.domain.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;

/**
 * 질문 CRUD 및 질문별 답변 목록 조회.
 * <p>
 * 질문별 답변 목록 노출 범위는 "면접 경험 1개 공유 → 모든 면접 정보 열람" 정책에 따라
 * {@link kr.hirekit.api.domain.answer.service.MemberAnswerAccessService}로 허용
 * visibility를 결정한다.
 */
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
    /** 공유 1개 이상 시 회원공개 답변 열람 허용 등 접근 정책 적용용 */
    private final MemberAnswerAccessService memberAnswerAccessService;

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
    public CursorPageResponse<AnswerListItemResponse> getAnswersByQuestionId(UUID questionId, UUID memberId, String cursor,
            Integer size) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
        if (question.getVisibility() != QuestionVisibility.PUBLIC || question.isForcedPrivate()) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }

        int pageSize = size != null ? Math.min(Math.max(1, size), MAX_ANSWER_PAGE_SIZE) : DEFAULT_ANSWER_PAGE_SIZE;
        // "면접 경험 1개 공유 → 모든 면접 정보 열람" 정책. 비로그인/공유 0개 → PUBLIC만, 공유 1개 이상 → PUBLIC +
        // MEMBERS_ONLY
        List<AnswerVisibility> allowedVisibilities = memberAnswerAccessService.getAllowedVisibilities(memberId);

        Optional<AnswerCursor> parsed = AnswerCursor.parse(cursor);
        var cursorCreatedAt = parsed.map(AnswerCursor::getCreatedAt).orElse(null);
        var cursorId = parsed.map(AnswerCursor::getAnswerId).orElse(null);

        List<Answer> answers = answerRepository.findAnswersByQuestionIdCursor(
                questionId, allowedVisibilities, memberId, cursorCreatedAt, cursorId, PageRequest.of(0, pageSize));

        if (answers.isEmpty()) {
            return CursorPageResponse.<AnswerListItemResponse>builder().items(List.of()).nextCursor(null).build();
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

        return CursorPageResponse.<AnswerListItemResponse>builder()
                .items(items)
                .nextCursor(nextCursor)
                .build();
    }

    @Override
    public MembersOnlyAnswerCountResponse getMembersOnlyAnswerCount(UUID questionId, UUID memberId) {
        List<AnswerVisibility> allowedVisibilities = memberAnswerAccessService.getAllowedVisibilities(memberId);
        if (allowedVisibilities.contains(AnswerVisibility.MEMBERS_ONLY)) {
            return MembersOnlyAnswerCountResponse.of(0);
        }
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
        if (question.getVisibility() != QuestionVisibility.PUBLIC || question.isForcedPrivate()) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }
        var countByVisibility = answerRepository.countByQuestionIdGroupByVisibility(List.of(questionId));
        long membersOnlyCount = countByVisibility
                .getOrDefault(questionId, Map.of())
                .getOrDefault(AnswerVisibility.MEMBERS_ONLY, 0L);
        return MembersOnlyAnswerCountResponse.of(membersOnlyCount);
    }

    @Override
    @Transactional
    public QuestionDetailResponse updateQuestion(UUID id, QuestionUpdateRequest request, UUID memberId) {
        Question question = questionRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
        if (!question.getAuthor().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (answerRepository.existsByQuestion_Id(id)) {
            throw new BusinessException(ErrorCode.QUESTION_HAS_ANSWERS);
        }
        question.update(request.getJob(), request.getContent(), request.getVisibility(), request.isAuthorHidden());
        return QuestionDetailResponse.from(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(UUID id, UUID memberId) {
        Question question = questionRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
        if (!question.getAuthor().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (answerRepository.existsByQuestion_Id(id)) {
            throw new BusinessException(ErrorCode.QUESTION_HAS_ANSWERS);
        }
        questionRepository.delete(question);
    }

    @Override
    @Transactional
    public QuestionDetailResponse updateQuestionVisibility(UUID id, QuestionVisibilityUpdateRequest request,
            UUID memberId) {
        Question question = questionRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
        if (!question.getAuthor().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (answerRepository.existsByQuestion_Id(id)) {
            throw new BusinessException(ErrorCode.QUESTION_HAS_ANSWERS);
        }
        question.updateVisibility(request.getVisibility());
        return QuestionDetailResponse.from(question);
    }
}
