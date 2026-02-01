package kr.hirekit.api.domain.company.dto;

import kr.hirekit.api.common.dto.PageRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanySearchRequest extends PageRequest {
    private String keyword;
    private String industry;

    @Builder
    public CompanySearchRequest(int page, int size, String sortBy, String sortDirection,
            String keyword, String industry) {
        super(page, size, sortBy, sortDirection);
        this.keyword = keyword;
        this.industry = industry;
    }
}
