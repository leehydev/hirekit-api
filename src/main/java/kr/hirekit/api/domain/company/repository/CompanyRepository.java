package kr.hirekit.api.domain.company.repository;

import kr.hirekit.api.domain.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    List<Company> findByMemberId(UUID memberId);
}
