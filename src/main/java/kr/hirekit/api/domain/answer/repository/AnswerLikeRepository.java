package kr.hirekit.api.domain.answer.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hirekit.api.domain.answer.entity.AnswerLike;

public interface AnswerLikeRepository extends JpaRepository<AnswerLike, UUID> {
}
