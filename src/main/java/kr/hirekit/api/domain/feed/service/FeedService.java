package kr.hirekit.api.domain.feed.service;

import java.time.LocalDateTime;
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
import kr.hirekit.api.domain.answer.service.MemberAnswerAccessService;
import kr.hirekit.api.domain.answer.repository.RepresentativeAnswerRow;
import kr.hirekit.api.common.dto.CursorPageResponse;
import kr.hirekit.api.domain.feed.dto.FeedItemResponse;
import kr.hirekit.api.domain.feed.dto.FeedAnswerCounts;
import kr.hirekit.api.domain.feed.dto.FeedAnswerSummary;
import kr.hirekit.api.domain.feed.dto.FeedCursor;
import kr.hirekit.api.domain.feed.dto.FeedQuestionSummary;
import kr.hirekit.api.domain.question.entity.Job;
import kr.hirekit.api.domain.question.entity.Question;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;
import kr.hirekit.api.domain.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;

/**
 * 피드 조회 서비스.
 * <p>
 * 전체공개 질문을 시간 역순으로 보여주며, 질문당 대표 답변 1개(좋아요 많은 순 → 최신순)와
 * 권한별 답변 수를 함께 반환한다. 무한스크롤을 위해 커서 기반 페이지네이션을 사용한다.
 * <p>
 * 대표 답변·노출 범위는 "면접 경험 1개 공유 → 모든 면접 정보 열람" 정책에 따라
 * {@link MemberAnswerAccessService}로 허용 visibility를 결정한 뒤 사용한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    /** 요청 시 size 미지정 시 사용하는 기본 페이지 크기 (한 번에 가져올 질문 개수) */
    private static final int DEFAULT_PAGE_SIZE = 20;
    /** 한 번에 조회할 수 있는 최대 개수. 이 값을 넘는 size 요청은 이 값으로 제한되어 과부하를 방지한다. */
    private static final int MAX_PAGE_SIZE = 50;

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    /** 공유 1개 이상 시 회원공개 열람 허용 등 접근 정책 적용용 */
    private final MemberAnswerAccessService memberAnswerAccessService;

    /**
     * 피드 목록을 커서 기반으로 조회한다 (무한스크롤용).
     * <p>
     * <b>질문 조건</b>: visibility=전체공개, forcedPrivate=false. 정렬은 createdAt DESC, id DESC.
     * <b>대표 답변</b>: 질문당 1개. 좋아요 많은 순 → 동점이면 최신순. 허용 visibility는 "면접 경험 1개 공유 시 회원공개 열람" 정책 적용.
     * <b>answerCounts</b>: 권한별 답변 수를 담아 UI에서 "1개 공유하고 N개 더 보기" 등에 활용할 수 있게 한다.
     *
     * @param companyId 회사 ID 필터 (null이면 전체 회사)
     * @param memberId  로그인한 회원 ID (null이면 비로그인 → 전체공개 답변만 노출)
     * @param job       직무 필터 (null이면 전체 직무)
     * @param cursor    이전 응답의 nextCursor. 첫 요청이면 null 또는 생략
     * @param size      한 페이지에 가져올 질문 수 (null이면 {@value #DEFAULT_PAGE_SIZE}, 최대 {@value #MAX_PAGE_SIZE})
     * @return items와 다음 페이지용 nextCursor (더 없으면 null)
     */
    public CursorPageResponse<FeedItemResponse> getFeed(UUID companyId, UUID memberId, Job job, String cursor, Integer size) {
        // size: null이면 기본값, 1 미만이면 1, MAX 초과면 MAX로 클램프
        int pageSize = size != null ? Math.min(Math.max(1, size), MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        PageRequest pageRequest = PageRequest.of(0, pageSize);

        // ─── 1) 피드용 질문 목록 조회 (QueryDSL 동적 쿼리) ─────────────────────────────
        // cursor 문자열 파싱: "createdAt_uuid" 형식. 첫 요청이거나 잘못된 값이면 empty → 첫 페이지 조회
        Optional<FeedCursor> parsed = FeedCursor.parse(cursor);
        LocalDateTime cursorCreatedAt = parsed.map(FeedCursor::getCreatedAt).orElse(null);
        UUID cursorId = parsed.map(FeedCursor::getQuestionId).orElse(null);

        // visibility=전체공개, forcedPrivate=false. companyId/job은 null이면 필터 미적용. 정렬: createdAt DESC, id DESC
        List<Question> questions = questionRepository.findFeedQuestions(
                QuestionVisibility.PUBLIC, companyId, job, cursorCreatedAt, cursorId, pageRequest);

        if (questions.isEmpty()) {
            return CursorPageResponse.<FeedItemResponse>builder().items(List.of()).nextCursor(null).build();
        }

        List<UUID> questionIds = questions.stream().map(Question::getId).toList();

        // ─── 2) 질문별 대표 답변 1개 + 권한별 답변 수 조회 ─────────────────────────────
        // "면접 경험 1개 공유 → 모든 면접 정보 열람" 정책 적용.
        // 비로그인 → [PUBLIC]. 로그인+공유 0개 → [PUBLIC]. 로그인+공유 1개 이상 → [PUBLIC, MEMBERS_ONLY].
        // (내부에서 해당 회원의 공유 답변 수 COUNT 후 허용 visibility 결정)
        List<AnswerVisibility> allowedVisibilities = memberAnswerAccessService.getAllowedVisibilities(memberId);

        // 질문당 대표 답변 1개: 좋아요 많은 순 → 동점이면 최신순. 위에서 정한 allowedVisibilities 내에서만 선정
        Map<UUID, RepresentativeAnswerRow> representativeMap =
                answerRepository.findRepresentativeAnswersByQuestionIds(questionIds, allowedVisibilities);
        // 질문별로 visibility(전체공개/회원공개/비공개)마다 답변 개수 → UI에서 "로그인하고 N개 더 보기" 등에 사용
        Map<UUID, Map<AnswerVisibility, Long>> countMap =
                answerRepository.countByQuestionIdGroupByVisibility(questionIds);

        // 대표 답변으로 선정된 Answer 엔티티만 ID로 한 번에 조회 (N+1 방지)
        List<UUID> representativeAnswerIds = representativeMap.values().stream()
                .map(RepresentativeAnswerRow::getAnswerId)
                .distinct()
                .toList();
        Map<UUID, Answer> answerMap = representativeAnswerIds.isEmpty()
                ? Map.of()
                : answerRepository.findAllById(representativeAnswerIds).stream()
                        .collect(java.util.stream.Collectors.toMap(Answer::getId, a -> a));

        // ─── 3) 질문 순서대로 FeedItemResponse 조립 ───────────────────────────────────
        // 각 아이템: 질문 요약 + 대표 답변 요약(없으면 null) + 권한별 답변 수
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

        // ─── 4) 다음 페이지 커서 생성 ─────────────────────────────────────────────────
        // 이번에 요청한 pageSize만큼 다 채워서 왔으면 다음 페이지가 있을 수 있으므로, 마지막 질문 기준으로 nextCursor 부여
        // 그보다 적게 왔으면 마지막 페이지이므로 nextCursor는 null
        Question last = questions.get(questions.size() - 1);
        String nextCursor = questions.size() == pageSize
                ? FeedCursor.encode(last.getCreatedAt(), last.getId())
                : null;

        return CursorPageResponse.<FeedItemResponse>builder()
                .items(items)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * visibility별 답변 개수 맵을 FeedAnswerCounts DTO로 변환한다.
     * <p>
     * totalAnswerCount는 전체(공개+회원공개+비공개) 합산. API에서는 publicAnswerCount, membersOnlyAnswerCount만
     * 노출하여 "로그인하면 N개 더 보기" 등 UI 연동에 사용한다.
     *
     * @param byVisibility visibility → 개수 맵 (없는 key는 0으로 간주)
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
