package kr.hirekit.api.domain.feed.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hirekit.api.domain.answer.entity.Answer;
import kr.hirekit.api.domain.answer.entity.AnswerVisibility;
import kr.hirekit.api.domain.answer.repository.AnswerRepository;
import kr.hirekit.api.domain.answer.repository.RepresentativeAnswerRow;
import kr.hirekit.api.domain.feed.dto.CursorFeedResponse;
import kr.hirekit.api.domain.feed.dto.FeedAnswerCounts;
import kr.hirekit.api.domain.feed.dto.FeedAnswerSummary;
import kr.hirekit.api.domain.feed.dto.FeedCursor;
import kr.hirekit.api.domain.feed.dto.FeedItemResponse;
import kr.hirekit.api.domain.feed.dto.FeedQuestionSummary;
import kr.hirekit.api.domain.question.entity.Question;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;
import kr.hirekit.api.domain.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;

/**
 * 피드 조회 서비스.
 * <p>
 * 전체공개 질문을 시간 역순으로 보여주며, 질문당 대표 답변 1개(좋아요 많은 순 → 최신순)와
 * 권한별 답변 수를 함께 반환한다. 무한스크롤을 위해 커서 기반 페이지네이션을 사용한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    /** 요청 시 size 미지정 시 사용하는 기본 페이지 크기 */
    private static final int DEFAULT_PAGE_SIZE = 20;
    /** 한 번에 조회할 수 있는 최대 개수 (과부하 방지) */
    private static final int MAX_PAGE_SIZE = 50;

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    /**
     * 피드 목록을 커서 기반으로 조회한다 (무한스크롤용).
     * <p>
     * <b>질문 조건</b>: visibility=전체공개, forcedPrivate=false. 정렬은 createdAt DESC, id DESC.
     * <b>대표 답변</b>: 질문당 1개. 좋아요 많은 순 → 동점이면 최신순. 비로그인 시 전체공개 답변만 후보, 로그인 시 전체공개+회원공개.
     * <b>answerCounts</b>: 권한별 답변 수를 담아 UI에서 "로그인하고 N개 더 보기" 등에 활용할 수 있게 한다.
     *
     * @param companyId 회사 ID 필터 (null이면 전체 회사)
     * @param memberId  로그인한 회원 ID (null이면 비로그인 → 전체공개 답변만 노출)
     * @param cursor    이전 응답의 nextCursor. 첫 요청이면 null 또는 생략
     * @param size      한 페이지에 가져올 질문 수 (null이면 {@value #DEFAULT_PAGE_SIZE}, 최대 {@value #MAX_PAGE_SIZE})
     * @return items와 다음 페이지용 nextCursor (더 없으면 null)
     */
    public CursorFeedResponse getFeed(UUID companyId, UUID memberId, String cursor, Integer size) {
        int pageSize = size != null ? Math.min(Math.max(1, size), MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        PageRequest pageRequest = PageRequest.of(0, pageSize);

        // 1) 커서 유무에 따라 질문 목록 조회 (첫 페이지 vs 다음 페이지)
        List<Question> questions;
        Optional<FeedCursor> parsed = FeedCursor.parse(cursor);
        if (parsed.isEmpty()) {
            questions = companyId != null
                    ? questionRepository.findByVisibilityAndForcedPrivateFalseAndCompanyIdOrderByCreatedAtDescIdDesc(
                            QuestionVisibility.PUBLIC, companyId, pageRequest)
                    : questionRepository.findByVisibilityAndForcedPrivateFalseOrderByCreatedAtDescIdDesc(
                            QuestionVisibility.PUBLIC, pageRequest);
        } else {
            FeedCursor c = parsed.get();
            questions = companyId != null
                    ? questionRepository.findNextByVisibilityAndForcedPrivateFalseAndCompanyId(
                            QuestionVisibility.PUBLIC, companyId, c.getCreatedAt(), c.getQuestionId(), pageRequest)
                    : questionRepository.findNextByVisibilityAndForcedPrivateFalse(
                            QuestionVisibility.PUBLIC, c.getCreatedAt(), c.getQuestionId(), pageRequest);
        }

        if (questions.isEmpty()) {
            return CursorFeedResponse.builder().items(List.of()).nextCursor(null).build();
        }

        List<UUID> questionIds = questions.stream().map(Question::getId).toList();

        // 2) 질문별 대표 답변 1개 + 권한별 답변 수 조회
        List<AnswerVisibility> allowedVisibilities = memberId != null
                ? List.of(AnswerVisibility.PUBLIC, AnswerVisibility.MEMBERS_ONLY)
                : List.of(AnswerVisibility.PUBLIC);

        Map<UUID, RepresentativeAnswerRow> representativeMap =
                answerRepository.findRepresentativeAnswersByQuestionIds(questionIds, allowedVisibilities);
        Map<UUID, Map<AnswerVisibility, Long>> countMap =
                answerRepository.countByQuestionIdGroupByVisibility(questionIds);

        List<UUID> representativeAnswerIds = representativeMap.values().stream()
                .map(RepresentativeAnswerRow::getAnswerId)
                .distinct()
                .toList();
        Map<UUID, Answer> answerMap = representativeAnswerIds.isEmpty()
                ? Map.of()
                : answerRepository.findAllById(representativeAnswerIds).stream()
                        .collect(java.util.stream.Collectors.toMap(Answer::getId, a -> a));

        // 3) 질문 순서대로 FeedItemResponse 조립 (질문 요약 + 대표 답변 + 답변 수)
        List<FeedItemResponse> items = new ArrayList<>();
        for (Question q : questions) {
            FeedQuestionSummary questionSummary = FeedQuestionSummary.from(q);
            FeedAnswerCounts counts = toFeedAnswerCounts(countMap.getOrDefault(q.getId(), Map.of()));

            RepresentativeAnswerRow row = representativeMap.get(q.getId());
            FeedAnswerSummary representativeSummary = null;
            if (row != null) {
                Answer answer = answerMap.get(row.getAnswerId());
                if (answer != null) {
                    representativeSummary = FeedAnswerSummary.from(answer, row.getLikeCount());
                }
            }

            items.add(FeedItemResponse.builder()
                    .question(questionSummary)
                    .representativeAnswer(representativeSummary)
                    .answerCounts(counts)
                    .build());
        }

        // 4) 다음 페이지가 있으면 마지막 질문 기준으로 nextCursor 생성
        Question last = questions.get(questions.size() - 1);
        String nextCursor = questions.size() == pageSize
                ? FeedCursor.encode(last.getCreatedAt(), last.getId())
                : null;

        return CursorFeedResponse.builder()
                .items(items)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * visibility별 답변 개수 맵을 FeedAnswerCounts DTO로 변환한다.
     *
     * @param byVisibility visibility → 개수
     * @return totalAnswerCount, publicAnswerCount, membersOnlyAnswerCount 포함
     */
    private static FeedAnswerCounts toFeedAnswerCounts(Map<AnswerVisibility, Long> byVisibility) {
        long publicCount = Optional.ofNullable(byVisibility.get(AnswerVisibility.PUBLIC)).orElse(0L);
        long membersOnlyCount = Optional.ofNullable(byVisibility.get(AnswerVisibility.MEMBERS_ONLY)).orElse(0L);
        long privateCount = Optional.ofNullable(byVisibility.get(AnswerVisibility.PRIVATE)).orElse(0L);
        return FeedAnswerCounts.builder()
                .totalAnswerCount(publicCount + membersOnlyCount + privateCount)
                .publicAnswerCount(publicCount)
                .membersOnlyAnswerCount(membersOnlyCount)
                .build();
    }
}
