package kr.hirekit.api.domain.company.repository;

import org.springframework.data.domain.Page;

import kr.hirekit.api.domain.company.dto.CompanySearchRequest;
import kr.hirekit.api.domain.company.entity.Company;

public interface CompanyRepositoryCustom {
    Page<Company> search(CompanySearchRequest request);
}
