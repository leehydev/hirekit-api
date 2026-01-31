package kr.hirekit.api.domain.company.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import kr.hirekit.api.auth.member.entity.Member;
import kr.hirekit.api.auth.member.repository.MemberRepository;
import kr.hirekit.api.client.PublicDataClient;
import kr.hirekit.api.common.dto.ErrorCode;
import kr.hirekit.api.common.exception.BusinessException;
import kr.hirekit.api.domain.company.dto.CompanyCreateRequest;
import kr.hirekit.api.domain.company.dto.CompanyResponse;
import kr.hirekit.api.domain.company.entity.Company;
import kr.hirekit.api.domain.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;
    private final PublicDataClient publicDataClient;

    /**
     * 기업 생성
     * 
     * @param request
     * @return
     */
    @Override
    public CompanyResponse createCompany(CompanyCreateRequest request, UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return CompanyResponse.from(companyRepository.save(Company.builder()
                .name(request.getName())
                .industry(request.getIndustry())
                .ceoName(request.getCeoName())
                .address(request.getAddress())
                .foundedDate(request.getFoundedDate())
                .businessNumber(request.getBusinessNumber())
                .registeredBy(member)
                .build()));
    }

    /**
     * DB에서 기업 조회
     * 
     * @param id
     * @return
     */
    @Override
    public CompanyResponse getCompany(UUID id) {
        return CompanyResponse.from(companyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND)));
    }

    /**
     * 공공데이터 API 기업 목록 조회
     * 
     * @param request
     * @return
     */
    @Override
    public Page<CompanyResponse> getCompanies(String name) {
        return CompanyResponse.pageFrom(publicDataClient.getCorpOutline(null, name));
    }
}
