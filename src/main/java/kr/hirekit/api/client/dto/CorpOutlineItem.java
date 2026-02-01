package kr.hirekit.api.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 금융위원회 기업기본정보 V2 - 기업개요 조회 API의 item 한 건
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CorpOutlineItem {

    @JsonProperty("crno")
    private String crno; // 법인등록번호

    @JsonProperty("corpNm")
    private String corpNm; // 법인명

    @JsonProperty("corpEnsnNm")
    private String corpEnsnNm; // 법인영문명

    @JsonProperty("enpPbanCmpyNm")
    private String enpPbanCmpyNm; // 기업공시회사명

    @JsonProperty("enpRprFnm")
    private String enpRprFnm; // 대표자명

    @JsonProperty("corpRegMrktDcd")
    private String corpRegMrktDcd; // 법인시장구분코드

    @JsonProperty("corpRegMrktDcdNm")
    private String corpRegMrktDcdNm; // 법인시장구분명

    @JsonProperty("corpDcd")
    private String corpDcd; // 법인업종코드

    @JsonProperty("corpDcdNm")
    private String corpDcdNm; // 법인업종명

    @JsonProperty("bzno")
    private String bzno; // 사업자등록번호

    @JsonProperty("enpOzpno")
    private String enpOzpno; // 우편번호

    @JsonProperty("enpBsadr")
    private String enpBsadr; // 본사주소

    @JsonProperty("enpDtadr")
    private String enpDtadr; // 상세주소

    @JsonProperty("enpHmpgUrl")
    private String enpHmpgUrl; // 홈페이지

    @JsonProperty("enpTlno")
    private String enpTlno; // 전화번호

    @JsonProperty("enpFxno")
    private String enpFxno; // 팩스번호

    @JsonProperty("sicNm")
    private String sicNm; // 산업분류명

    @JsonProperty("enpEstbDt")
    private String enpEstbDt; // 설립일

    @JsonProperty("enpStacMm")
    private String enpStacMm; // 결산월

    @JsonProperty("enpXchgLstgDt")
    private String enpXchgLstgDt;

    @JsonProperty("enpXchgLstgAbolDt")
    private String enpXchgLstgAbolDt;

    @JsonProperty("enpKosdaqLstgDt")
    private String enpKosdaqLstgDt;

    @JsonProperty("enpKosdaqLstgAbolDt")
    private String enpKosdaqLstgAbolDt;

    @JsonProperty("enpKrxLstgDt")
    private String enpKrxLstgDt;

    @JsonProperty("enpKrxLstgAbolDt")
    private String enpKrxLstgAbolDt;

    @JsonProperty("smenpYn")
    private String smenpYn; // 중소기업여부

    @JsonProperty("enpMntrBnkNm")
    private String enpMntrBnkNm; // 주담당은행명

    @JsonProperty("enpEmpeCnt")
    private String enpEmpeCnt; // 종업원수

    @JsonProperty("empeAvgCnwkTermCtt")
    private String empeAvgCnwkTermCtt;

    @JsonProperty("enpPn1AvgSlryAmt")
    private String enpPn1AvgSlryAmt;

    @JsonProperty("actnAudpnNm")
    private String actnAudpnNm; // 회계감사인명

    @JsonProperty("audtRptOpnnCtt")
    private String audtRptOpnnCtt;

    @JsonProperty("enpMainBizNm")
    private String enpMainBizNm; // 주요사업내용

    @JsonProperty("fssCorpUnqNo")
    private String fssCorpUnqNo; // 금감원법인고유번호

    @JsonProperty("fssCorpChgDtm")
    private String fssCorpChgDtm; // 금감원변경일시

    @JsonProperty("fstOpegDt")
    private String fstOpegDt; // 최초개업일자

    @JsonProperty("lastOpegDt")
    private String lastOpegDt; // 최종개업일자
}
