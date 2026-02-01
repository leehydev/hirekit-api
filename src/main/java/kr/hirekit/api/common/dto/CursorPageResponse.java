package kr.hirekit.api.common.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커서 기반 페이징 공통 응답 (무한스크롤).
 * - items: 이번에 조회된 목록
 * - nextCursor: 다음 페이지 요청 시 사용할 커서 (없으면 null)
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CursorPageResponse<T> {

    private List<T> items;
    /** 다음 페이지 커서. 없으면 null */
    private String nextCursor;
}
