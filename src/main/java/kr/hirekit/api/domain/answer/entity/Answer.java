package kr.hirekit.api.domain.answer.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import kr.hirekit.api.auth.member.entity.Member;
import kr.hirekit.api.common.entity.BaseEntity;
import kr.hirekit.api.domain.question.entity.Question;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answers")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer extends BaseEntity {

    /**
     * 질문
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * 답변 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 팁
     */
    @Column(columnDefinition = "TEXT")
    private String tip;

    /**
     * 합격 여부 (아직 결과 없으면 null)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "pass_status", length = 20)
    private PassStatus passStatus;

    /**
     * 면접 시기 (아직 면접 전이면 null)
     */
    @Column(name = "interview_date")
    private LocalDate interviewDate;

    /**
     * 작성자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    /**
     * 메인 피드 공개 여부 (전체공개, 회원만 공개, 비공개)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnswerVisibility visibility;

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
