package kr.hirekit.api.domain.answer.entity;

import jakarta.persistence.*;
import kr.hirekit.api.auth.member.entity.Member;
import kr.hirekit.api.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answer_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"answer_id", "member_id"}))
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerLike extends BaseEntity {

    /**
     * 답변
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    /**
     * 좋아요를 누른 회원
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
