package kr.hirekit.api.domain.company.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import kr.hirekit.api.common.dto.OffsetPageResponse;
import kr.hirekit.api.common.dto.PageRequest;
import kr.hirekit.api.domain.company.dto.CompanyCreateRequest;
import kr.hirekit.api.domain.company.dto.CompanyResponse;

public interface CompanyService {
    /**
     * 기업 생성
     * 
     * @param request
     * @return
     */
    CompanyResponse createCompany(CompanyCreateRequest request, UUID memberId);

    /**
     * 기업 조회
     * 
     * @param id
     * @return
     */
    CompanyResponse getCompany(UUID id);

    /**
     * 기업 목록 조회 (공공데이터 기업개요 API 연동)
     *
     * @param name 법인명
     * @return 기업 목록 페이지
     */
    Page<CompanyResponse> getCompanies(String name);

    /**
     * 기업 검색 (내부 DB). 등록된 기업만 이름·업종 기준으로 검색.
     *
     * @param keyword     검색어 (기업명, null/blank면 미적용)
     * @param industry   업종 필터 (null/blank면 미적용)
     * @param pageRequest 페이지, 크기, 정렬
     * @return 검색 결과 페이지
     */
    OffsetPageResponse<CompanyResponse> searchCompanies(String keyword, String industry, PageRequest pageRequest);
}
