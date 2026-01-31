package kr.hirekit.api.domain.company.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

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
}
