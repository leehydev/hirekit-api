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
import kr.hirekit.api.domain.answer.entity.Answer;
import kr.hirekit.api.domain.answer.entity.AnswerLike;
import kr.hirekit.api.domain.answer.repository.AnswerLikeRepository;
import kr.hirekit.api.domain.answer.repository.AnswerRepository;
import kr.hirekit.api.domain.question.entity.Question;
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

    @Override
    @Transactional
    public AnswerDetailResponse createAnswer(UUID questionId, AnswerCreateRequest request, UUID memberId) {
        Question question = questionRepository.findById(questionId)
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
