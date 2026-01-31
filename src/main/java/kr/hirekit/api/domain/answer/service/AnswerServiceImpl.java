package kr.hirekit.api.domain.answer.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hirekit.api.auth.member.entity.Member;
import kr.hirekit.api.auth.member.repository.MemberRepository;
import kr.hirekit.api.common.dto.ErrorCode;
import kr.hirekit.api.common.exception.BusinessException;
import kr.hirekit.api.domain.answer.dto.AnswerCreateRequest;
import kr.hirekit.api.domain.answer.dto.AnswerDetailResponse;
import kr.hirekit.api.domain.answer.dto.AnswerLikeToggleResponse;
import kr.hirekit.api.domain.answer.dto.AnswerUpdateRequest;
import kr.hirekit.api.domain.answer.dto.AnswerVisibilityUpdateRequest;
import kr.hirekit.api.domain.answer.entity.Answer;
import kr.hirekit.api.domain.answer.entity.AnswerLike;
import kr.hirekit.api.domain.answer.repository.AnswerLikeRepository;
import kr.hirekit.api.domain.answer.repository.AnswerRepository;
import kr.hirekit.api.domain.question.entity.Question;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;
import kr.hirekit.api.domain.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final AnswerLikeRepository answerLikeRepository;
    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final MemberAnswerAccessService memberAnswerAccessService;

    @Override
    @Transactional
    public AnswerDetailResponse createAnswer(UUID questionId, AnswerCreateRequest request, UUID memberId) {
        // 질문 행 락: 삭제/수정과 동시에 답변 등록 시 레이스 방지
        Question question = questionRepository.findByIdForUpdate(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Answer answer = answerRepository.save(Answer.builder()
                .question(question)
                .content(request.getContent())
                .tip(request.getTip())
                .passStatus(request.getPassStatus())
                .interviewDate(request.getInterviewDate())
                .author(author)
                .visibility(request.getVisibility())
                .authorHidden(request.isAuthorHidden())
                .forcedPrivate(false)
                .build());
        return AnswerDetailResponse.from(answer);
    }

    @Override
    public AnswerDetailResponse getAnswer(UUID questionId, UUID answerId, UUID memberId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANSWER_NOT_FOUND));
        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new BusinessException(ErrorCode.ANSWER_NOT_FOUND);
        }
        Question question = answer.getQuestion();
        if (question.getVisibility() != QuestionVisibility.PUBLIC || question.isForcedPrivate()) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }
        if (answer.isForcedPrivate()) {
            throw new BusinessException(ErrorCode.ANSWER_NOT_FOUND);
        }
        var allowedVisibilities = memberAnswerAccessService.getAllowedVisibilities(memberId);
        if (!allowedVisibilities.contains(answer.getVisibility())) {
            throw new BusinessException(ErrorCode.ANSWER_NOT_FOUND);
        }
        return AnswerDetailResponse.from(answer);
    }

    @Override
    @Transactional
    public AnswerDetailResponse updateAnswer(UUID questionId, UUID answerId, AnswerUpdateRequest request,
            UUID memberId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANSWER_NOT_FOUND));
        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new BusinessException(ErrorCode.ANSWER_NOT_FOUND);
        }
        if (!answer.getAuthor().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        answer.update(request.getContent(), request.getTip(), request.getPassStatus(),
                request.getInterviewDate(), request.getVisibility(), request.isAuthorHidden());
        return AnswerDetailResponse.from(answer);
    }

    @Override
    @Transactional
    public void deleteAnswer(UUID questionId, UUID answerId, UUID memberId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANSWER_NOT_FOUND));
        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new BusinessException(ErrorCode.ANSWER_NOT_FOUND);
        }
        if (!answer.getAuthor().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        answerRepository.delete(answer);
    }

    @Override
    @Transactional
    public AnswerDetailResponse updateAnswerVisibility(UUID questionId, UUID answerId,
            AnswerVisibilityUpdateRequest request, UUID memberId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANSWER_NOT_FOUND));
        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new BusinessException(ErrorCode.ANSWER_NOT_FOUND);
        }
        if (!answer.getAuthor().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        answer.updateVisibility(request.getVisibility());
        return AnswerDetailResponse.from(answer);
    }

    @Override
    @Transactional
    public AnswerLikeToggleResponse toggleLike(UUID questionId, UUID answerId, UUID memberId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANSWER_NOT_FOUND));
        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new BusinessException(ErrorCode.ANSWER_NOT_FOUND);
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        boolean liked;
        var existing = answerLikeRepository.findByAnswerIdAndMemberId(answerId, memberId);
        if (existing.isPresent()) {
            answerLikeRepository.delete(existing.get());
            liked = false;
        } else {
            answerLikeRepository.save(AnswerLike.builder()
                    .answer(answer)
                    .member(member)
                    .build());
            liked = true;
        }
        long likeCount = answerLikeRepository.countByAnswerId(answerId);
        return AnswerLikeToggleResponse.builder()
                .liked(liked)
                .likeCount(likeCount)
                .build();
    }
}
