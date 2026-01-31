package kr.hirekit.api.domain.answer.repository;

import java.util.Collection;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.hirekit.api.domain.answer.entity.Answer;
import kr.hirekit.api.domain.answer.entity.AnswerVisibility;

public interface AnswerRepository extends JpaRepository<Answer, UUID>, AnswerRepositoryCustom {

    /**
     * 해당 질문에 답변이 하나라도 있는지 여부.
     */
    boolean existsByQuestion_Id(UUID questionId);

    /**
     * 해당 회원이 "공유한" 답변 수를 센다.
     * <p>
     * "면접 경험 1개 공유" 정책에서 "공유"의 정의와 동일하다.
     * <ul>
     *   <li>author_id = authorId</li>
     *   <li>visibility IN (visibilities에 전달한 값들, 보통 PUBLIC, MEMBERS_ONLY)</li>
     *   <li>forced_private = false</li>
     * </ul>
     * 비공개(PRIVATE)·강제 비공개 답변은 제외된다. 반환값이 1 이상이면 해당 회원은 회원공개 답변까지 열람 가능.
     *
     * @param authorId    답변 작성자(회원) ID
     * @param visibilities 공유로 인정할 visibility 목록 (보통 PUBLIC, MEMBERS_ONLY)
     * @return 조건을 만족하는 답변 개수 (0 이상)
     */
    long countByAuthor_IdAndVisibilityInAndForcedPrivateFalse(UUID authorId, Collection<AnswerVisibility> visibilities);
}
