package kr.hirekit.api.domain.question.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import kr.hirekit.api.domain.question.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, UUID>, QuestionRepositoryCustom {

    /**
     * 질문 단건 조회 + 비관적 락(PESSIMISTIC_WRITE).
     * 수정/삭제/공개상태변경과 답변 등록의 동시성 제어용.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM Question q WHERE q.id = :id")
    Optional<Question> findByIdForUpdate(@Param("id") UUID id);
}
