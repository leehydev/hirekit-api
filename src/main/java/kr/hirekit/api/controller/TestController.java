package kr.hirekit.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hirekit.api.client.PublicDataClient;
import kr.hirekit.api.client.dto.CorpOutlineApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test", description = "공공데이터 API 테스트")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final PublicDataClient publicDataClient;

    @Operation(summary = "기업 개요 조회", description = "금융위원회 기업기본정보 V2 API를 통해 기업 개요를 조회합니다.")
    @GetMapping("/corp-outline")
    public ResponseEntity<CorpOutlineApiResponse> getCorpOutline(
            @Parameter(description = "법인등록번호", example = "1101113892240") @RequestParam(name = "crno", required = false) String crno,
            @Parameter(description = "법인명", example = "메리츠자산운용") @RequestParam(name = "corpNm", required = false) String corpNm) {

        CorpOutlineApiResponse result = publicDataClient.getCorpOutline(crno, corpNm);
        return ResponseEntity.ok(result);
    }
}
