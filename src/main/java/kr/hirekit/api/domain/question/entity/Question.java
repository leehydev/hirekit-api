package kr.hirekit.api.domain.question.entity;

import jakarta.persistence.*;
import kr.hirekit.api.auth.member.entity.Member;
import kr.hirekit.api.common.entity.BaseEntity;
import kr.hirekit.api.domain.company.entity.Company;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseEntity {

    /**
     * 회사
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /**
     * 직무
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Job job;

    /**
     * 질문 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 작성자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    /**
     * 메인 피드 공개 여부 (전체공개, 비공개)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionVisibility visibility;

    /**
     * 작성자 숨김 여부
     */
    @Column(name = "author_hidden", nullable = false)
    private boolean authorHidden;

    /**
     * 강제 비공개 처리 여부 (신고, 이상 내용 등)
     */
    @Column(name = "forced_private", nullable = false)
    private boolean forcedPrivate;
}
