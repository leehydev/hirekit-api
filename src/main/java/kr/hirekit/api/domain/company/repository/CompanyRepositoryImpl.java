package kr.hirekit.api.domain.company.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import kr.hirekit.api.domain.company.dto.CompanySearchRequest;
import kr.hirekit.api.domain.company.entity.Company;
import kr.hirekit.api.domain.company.entity.QCompany;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Company> search(CompanySearchRequest request) {
        QCompany company = QCompany.company;

        List<Company> content = queryFactory
                .selectFrom(company)
                .where(
                        keywordContains(request.getKeyword()),
                        industryEq(request.getIndustry()))
                .orderBy(getOrderSpecifier(request))
                .offset(request.getPage() * request.getSize())
                .limit(request.getSize())
                .fetch();

        Long total = queryFactory
                .select(company.count())
                .from(company)
                .where(
                        keywordContains(request.getKeyword()),
                        industryEq(request.getIndustry()))
                .fetchOne();

        return new PageImpl<>(content, request.toPageable(), total != null ? total : 0L);
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword)
                ? QCompany.company.name.contains(keyword)
                : null;
    }

    private BooleanExpression industryEq(String industry) {
        return StringUtils.hasText(industry)
                ? QCompany.company.industry.eq(industry)
                : null;
    }

    private OrderSpecifier<?> getOrderSpecifier(CompanySearchRequest request) {
        QCompany company = QCompany.company;
        Order order = request.getSortDirection().equalsIgnoreCase("desc") ? Order.DESC : Order.ASC;

        return switch (request.getSortBy()) {
            case "createdAt" -> new OrderSpecifier<>(order, company.createdAt);
            case "industry" -> new OrderSpecifier<>(order, company.industry);
            default -> new OrderSpecifier<>(order, company.name);
        };
    }
}