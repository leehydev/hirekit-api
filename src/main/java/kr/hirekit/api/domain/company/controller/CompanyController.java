package kr.hirekit.api.domain.company.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Operation(summary = "기업 목록 조회", description = "법인명으로 공공데이터 기업개요 API를 조회해 기업 목록을 페이지 단위로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<Page<CompanyResponse>> search(
            @Parameter(description = "검색할 법인명(기업명)", example = "메리츠자산운용", required = true) @RequestParam String name) {
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
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID memberId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyService.createCompany(request, memberId));
    }
}
