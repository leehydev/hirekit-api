package kr.hirekit.api.domain.answer.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hirekit.api.domain.answer.entity.AnswerVisibility;
import kr.hirekit.api.domain.answer.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;

/**
 * "나의 면접 경험 1개 공유 → 모든 면접 정보 열람" 정책을 담당하는 서비스.
 * <p>
 * 피드·질문별 답변 목록 등에서 "이 요청자가 볼 수 있는 답변의 visibility"를 결정할 때
 * 이 서비스의 {@link #getAllowedVisibilities(UUID)}를 사용한다.
 * <p>
 * <b>정책 요약</b>
 * <ul>
 *   <li>비로그인: 전체공개(PUBLIC) 답변만 열람 가능.</li>
 *   <li>로그인 + 공유한 답변 0개: 전체공개만 열람. (회원공개 답변은 막힘)</li>
 *   <li>로그인 + 공유한 답변 1개 이상: 전체공개 + 회원공개(MEMBERS_ONLY) 열람. ("모든 면접 정보"에 해당)</li>
 * </ul>
 * "공유한 답변" = 해당 회원이 작성한 답변 중 visibility가 PUBLIC 또는 MEMBERS_ONLY이고,
 * forced_private=false인 것. (비공개·강제 비공개는 카운트하지 않음)
 * <p>
 * 구현 방식: 조회 시마다 answers 테이블에서 COUNT 쿼리로 공유 답변 수를 계산한다.
 * 스키마 변경 없이 정합성을 유지할 수 있고, 답변 삭제·visibility 변경 시 별도 동기화가 필요 없다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAnswerAccessService {

    /** 비로그인 또는 공유 0개 회원이 볼 수 있는 visibility (전체공개만) */
    private static final List<AnswerVisibility> PUBLIC_ONLY = List.of(AnswerVisibility.PUBLIC);
    /** 공유 1개 이상 회원이 볼 수 있는 visibility (전체공개 + 회원공개) */
    private static final List<AnswerVisibility> PUBLIC_AND_MEMBERS_ONLY =
            List.of(AnswerVisibility.PUBLIC, AnswerVisibility.MEMBERS_ONLY);

    private final AnswerRepository answerRepository;

    /**
     * 요청 회원이 열람할 수 있는 답변의 visibility 목록을 반환한다.
     * <p>
     * 피드의 대표 답변 선정, 질문별 답변 목록 조회 등에서 "어떤 visibility까지 노출할지"를
     * 결정할 때 이 목록을 사용한다. (예: Repository에 allowedVisibilities로 전달)
     *
     * @param memberId 로그인한 회원 ID. 비로그인 요청이면 null.
     * @return 허용 visibility 목록. [PUBLIC] 또는 [PUBLIC, MEMBERS_ONLY]. null/빈 리스트 아님.
     */
    public List<AnswerVisibility> getAllowedVisibilities(UUID memberId) {
        if (memberId == null) {
            return PUBLIC_ONLY;
        }
        // 공유한 답변 수: author=memberId 이고 visibility in (PUBLIC, MEMBERS_ONLY), forced_private=false
        long sharedCount = answerRepository.countByAuthor_IdAndVisibilityInAndForcedPrivateFalse(
                memberId, List.of(AnswerVisibility.PUBLIC, AnswerVisibility.MEMBERS_ONLY));
        return sharedCount >= 1 ? PUBLIC_AND_MEMBERS_ONLY : PUBLIC_ONLY;
    }
}
