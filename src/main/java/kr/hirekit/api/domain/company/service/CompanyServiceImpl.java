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
     * 기업 등록. 사업자번호로 이미 등록된 회사가 있으면 해당 회사를 반환하고,
     * 없으면 새 회사를 저장한 뒤 반환한다.
     *
     * @param request  등록할 기업 정보
     * @param memberId 등록 요청 회원 ID
     * @return 기존 회사 또는 새로 저장된 회사의 응답
     */
    @Override
    public CompanyResponse createCompany(CompanyCreateRequest request, UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 사업자번호로 기존 회사 조회 → 있으면 그대로 반환, 없으면 새로 저장 후 반환
        return companyRepository.findByBusinessNumber(request.getBusinessNumber())
                .map(CompanyResponse::from)
                .orElseGet(() -> CompanyResponse.from(companyRepository.save(Company.builder()
                        .name(request.getName())
                        .industry(request.getIndustry())
                        .ceoName(request.getCeoName())
                        .address(request.getAddress())
                        .foundedDate(request.getFoundedDate())
                        .businessNumber(request.getBusinessNumber())
                        .registeredBy(member)
                        .build())));
    }

    /**
     * ID로 기업 단건 조회.
     *
     * @param id 기업 ID
     * @return 기업 응답
     */
    @Override
    public CompanyResponse getCompany(UUID id) {
        return CompanyResponse.from(companyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND)));
    }

    /**
     * 법인명으로 공공데이터 API 기업 목록 조회.
     *
     * @param name 검색할 법인명(기업명)
     * @return 기업 목록(페이지)
     */
    @Override
    public Page<CompanyResponse> getCompanies(String name) {
        return CompanyResponse.pageFrom(publicDataClient.getCorpOutline(null, name));
    }
}
