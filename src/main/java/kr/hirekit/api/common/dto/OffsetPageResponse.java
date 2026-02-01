package kr.hirekit.api.common.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오프셋(페이지 번호) 기반 페이징 공통 응답.
 * - content: 현재 페이지 목록
 * - totalElements: 전체 검색 결과 개수
 * - totalPages: 전체 페이지 수
 * - page: 현재 페이지 번호 (0-based)
 * - size: 페이지 크기
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OffsetPageResponse<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
    private boolean first;
    private boolean last;

    /**
     * Spring Data {@link org.springframework.data.domain.Page}로부터 공통 응답 생성.
     */
    public static <T> OffsetPageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return OffsetPageResponse.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
