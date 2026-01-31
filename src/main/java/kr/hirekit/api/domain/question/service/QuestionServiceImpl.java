package kr.hirekit.api.domain.question.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hirekit.api.common.dto.ErrorCode;
import kr.hirekit.api.common.exception.BusinessException;
import kr.hirekit.api.domain.question.dto.QuestionDetailResponse;
import kr.hirekit.api.domain.question.entity.Question;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;
import kr.hirekit.api.domain.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    @Override
    public QuestionDetailResponse getQuestion(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        if (question.getVisibility() != QuestionVisibility.PUBLIC || question.isForcedPrivate()) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }

        return QuestionDetailResponse.from(question);
    }
}
