package kr.hirekit.api.domain.company.entity;

import java.time.LocalDate;
import java.util.Map;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import kr.hirekit.api.auth.member.entity.Member;
import kr.hirekit.api.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "companies")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    /**
     * 기업명
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 업종
     */
    @Column(length = 50)
    private String industry;

    /**
     * 대표자명
     */
    @Column(name = "ceo_name", length = 50)
    private String ceoName;

    /**
     * 주소
     */
    private String address;

    /**
     * 설립일
     */
    @Column(name = "founded_date")
    private LocalDate foundedDate;

    /**
     * 사업자등록번호
     */
    @Column(name = "business_number", length = 20, unique = true)
    private String businessNumber;

    /**
     * 데이터 출처
     */
    @Column(name = "api_source", length = 30)
    private String apiSource;

    /**
     * 원본 데이터
     */
    @Type(JsonType.class)
    @Column(name = "api_raw_data", columnDefinition = "jsonb")
    private Map<String, Object> apiRawData;

    /**
     * 등록자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by")
    private Member registeredBy;

}
