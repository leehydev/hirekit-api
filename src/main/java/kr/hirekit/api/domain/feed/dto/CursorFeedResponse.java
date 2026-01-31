package kr.hirekit.api.domain.feed.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커서 기반 피드 응답 (무한스크롤).
 * - items: 이번에 조회된 피드 목록
 * - nextCursor: 다음 페이지 요청 시 사용할 커서 (없으면 null)
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CursorFeedResponse {

    private List<FeedItemResponse> items;
    /** 다음 페이지 커서. 없으면 null */
    private String nextCursor;
}
