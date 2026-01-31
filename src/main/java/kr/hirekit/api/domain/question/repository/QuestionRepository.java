package kr.hirekit.api.domain.question.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hirekit.api.domain.question.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, UUID>, QuestionRepositoryCustom {
}
