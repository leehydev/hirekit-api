package kr.hirekit.api.domain.company.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hirekit.api.common.dto.OffsetPageResponse;
import kr.hirekit.api.common.dto.PageRequest;
import kr.hirekit.api.domain.company.dto.CompanyCreateRequest;
import kr.hirekit.api.domain.company.dto.CompanyResponse;
import kr.hirekit.api.domain.company.service.CompanyService;
import lombok.RequiredArgsConstructor;

@Tag(name = "기업", description = "기업 검색 및 등록 API")
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "기업 검색 (내부 DB)", description = "등록된 기업을 기업명·업종으로 검색. 오프셋 페이징. 해당 회사의 질문만 보려면 검색한 회사 ID로 질문/피드 API에 companyId 전달.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<OffsetPageResponse<CompanyResponse>> searchCompanies(
            @Parameter(description = "검색어 (기업명, 미입력 시 전체)") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "업종 필터") @RequestParam(name = "industry", required = false) String industry,
            @Parameter(description = "페이지 번호 (0-based)") @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
            @Parameter(description = "정렬 필드 (name, industry, createdAt)") @RequestParam(name = "sortBy", required = false, defaultValue = "name") String sortBy,
            @Parameter(description = "정렬 방향 (asc, desc)") @RequestParam(name = "sortDirection", required = false, defaultValue = "asc") String sortDirection) {
        PageRequest pageRequest = new PageRequest(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(companyService.searchCompanies(keyword, industry, pageRequest));
    }

    @Operation(summary = "기업 목록 조회 (공공 API)", description = "법인명으로 공공데이터 기업개요 API를 조회해 기업 목록을 페이지 단위로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<Page<CompanyResponse>> getCompaniesFromPublicApi(
            @Parameter(description = "검색할 법인명(기업명)", example = "메리츠자산운용", required = true) @RequestParam(required = true, name = "name") String name) {
        return ResponseEntity.ok(companyService.getCompanies(name));
    }

    @Operation(summary = "기업 등록", description = "로그인한 회원이 새 기업을 등록합니다. 등록자는 해당 기업의 등록자로 저장됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<CompanyResponse> register(
            @RequestBody @Valid CompanyCreateRequest request,
            @AuthenticationPrincipal UUID memberId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyService.createCompany(request, memberId));
    }
}
