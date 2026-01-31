package kr.hirekit.api.domain.answer.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hirekit.api.domain.answer.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, UUID>, AnswerRepositoryCustom {
}
