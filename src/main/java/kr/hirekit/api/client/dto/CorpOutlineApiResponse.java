package kr.hirekit.api.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 금융위원회 기업기본정보 V2 - 기업개요 조회 API 최상위 응답
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CorpOutlineApiResponse {

    @JsonProperty("response")
    private CorpOutlineResponse response;
}
