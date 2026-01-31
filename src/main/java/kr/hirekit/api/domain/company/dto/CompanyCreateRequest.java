package kr.hirekit.api.domain.company.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CompanyCreateRequest {
    @NotBlank
    private String name;
    private String industry;
    private String ceoName;
    private String address;
    private LocalDate foundedDate;
    private String businessNumber;
}
