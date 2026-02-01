package kr.hirekit.api.domain.company.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import kr.hirekit.api.client.dto.CorpOutlineApiResponse;
import kr.hirekit.api.client.dto.CorpOutlineItem;
import kr.hirekit.api.client.dto.CorpOutlineItems;
import kr.hirekit.api.common.dto.BaseResponse;
import kr.hirekit.api.domain.company.entity.Company;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyResponse extends BaseResponse {
    private String name;
    private String industry;
    private String ceoName;
    private String address;
    private LocalDate foundedDate;
    private String businessNumber;
    private UUID registeredById;

    @Builder
    public CompanyResponse(UUID id, LocalDateTime createdAt, LocalDateTime updatedAt,
            String name, String industry, String ceoName, String address,
            LocalDate foundedDate, String businessNumber, UUID registeredById) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.industry = industry;
        this.ceoName = ceoName;
        this.address = address;
        this.foundedDate = foundedDate;
        this.businessNumber = businessNumber;
        this.registeredById = registeredById;
    }

    public static CompanyResponse from(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .name(company.getName())
                .industry(company.getIndustry())
                .ceoName(company.getCeoName())
                .address(company.getAddress())
                .foundedDate(company.getFoundedDate())
                .businessNumber(company.getBusinessNumber())
                .registeredById(company.getRegisteredBy() != null ? company.getRegisteredBy().getId() : null)
                .build();
    }

    /**
     * 공공데이터 기업개요 API 응답 item → CompanyResponse 매핑
     * (id, createdAt, updatedAt, registeredById는 null)
     */
    public static CompanyResponse from(CorpOutlineItem item) {
        LocalDate foundedDate = null;
        if (item.getEnpEstbDt() != null && !item.getEnpEstbDt().isBlank()) {
            try {
                foundedDate = LocalDate.parse(item.getEnpEstbDt(), DateTimeFormatter.BASIC_ISO_DATE);
            } catch (Exception ignored) {
                // 파싱 실패 시 null 유지
            }
        }
        return CompanyResponse.builder()
                .id(null)
                .createdAt(null)
                .updatedAt(null)
                .name(item.getCorpNm())
                .industry(item.getSicNm() != null ? item.getSicNm() : "")
                .ceoName(item.getEnpRprFnm())
                .address(item.getEnpBsadr())
                .foundedDate(foundedDate)
                .businessNumber(item.getBzno())
                .registeredById(null)
                .build();
    }

    /**
     * 공공데이터 기업개요 API 응답 전체 → Page&lt;CompanyResponse&gt; 변환
     */
    public static Page<CompanyResponse> pageFrom(CorpOutlineApiResponse apiResponse) {
        if (apiResponse.getResponse() == null
                || apiResponse.getResponse().getBody() == null
                || apiResponse.getResponse().getBody().getItems() == null) {
            return new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        }
        CorpOutlineItems items = apiResponse.getResponse().getBody().getItems();
        // 같은 사업자번호(bzno)면 lastOpegDt(최종개업일)가 가장 최근인 항목만 사용
        List<CompanyResponse> content = items.getItemSafe().stream()
                .collect(Collectors.groupingBy(item -> item.getBzno() != null ? item.getBzno() : ""))
                .values().stream()
                .map(group -> group.stream()
                        .max(Comparator.comparing(
                                item -> item.getLastOpegDt() != null && !item.getLastOpegDt().isBlank()
                                        ? item.getLastOpegDt()
                                        : ""))
                        .orElseThrow())
                .map(CompanyResponse::from)
                .collect(Collectors.toList());
        int pageNo = items.getPageNo() != null ? items.getPageNo() : 1;
        int size = items.getNumOfRows() != null ? items.getNumOfRows() : 10;
        long total = items.getTotalCount() != null ? items.getTotalCount() : 0;
        return new PageImpl<>(content, PageRequest.of(pageNo - 1, size), total);
    }
}