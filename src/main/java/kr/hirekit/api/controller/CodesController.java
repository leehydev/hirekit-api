package kr.hirekit.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hirekit.api.common.dto.CodeGroupResponse;
import kr.hirekit.api.common.service.CodesService;
import lombok.RequiredArgsConstructor;

@Tag(name = "코드", description = "코드/라벨 조회 (인증 불필요)")
@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CodesController {

    private final CodesService codesService;

    @Operation(summary = "코드 그룹 전체 조회", description = "CodeEnum 구현 enum들을 type별 codes 배열로 반환. 프론트 셀렉트/폼 등에서 사용.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<CodeGroupResponse>> getCodes() {
        List<CodeGroupResponse> groups = codesService.getAllCodeGroups();
        return ResponseEntity.ok(groups);
    }
}
