package kr.hirekit.api.domain.answer.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hirekit.api.domain.answer.dto.AnswerCreateRequest;
import kr.hirekit.api.domain.answer.dto.AnswerDetailResponse;
import kr.hirekit.api.domain.answer.service.AnswerService;
import lombok.RequiredArgsConstructor;

@Tag(name = "답변", description = "답변 등록 API")
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @Operation(summary = "답변 등록", description = "로그인한 회원이 특정 질문에 답변을 등록합니다. 등록자는 해당 답변의 작성자로 저장됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "질문 또는 회원을 찾을 수 없음")
    })
    @PostMapping("/{questionId}/answers")
    public ResponseEntity<AnswerDetailResponse> createAnswer(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable("questionId") UUID questionId,
            @RequestBody @Valid AnswerCreateRequest request,
            @AuthenticationPrincipal UUID memberId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(answerService.createAnswer(questionId, request, memberId));
    }
}
