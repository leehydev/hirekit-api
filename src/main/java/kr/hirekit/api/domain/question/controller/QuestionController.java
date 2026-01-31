package kr.hirekit.api.domain.question.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hirekit.api.domain.question.dto.QuestionDetailResponse;
import kr.hirekit.api.domain.question.service.QuestionService;
import lombok.RequiredArgsConstructor;

@Tag(name = "질문", description = "질문 조회 API")
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "질문 단건 조회", description = "전체공개이며 강제 비공개가 아닌 질문만 조회 가능. 그 외는 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음(비공개/강제비공개 포함)")
    })
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponse> getQuestion(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable(name = "id") String questionUuid) {
        UUID id = UUID.fromString(questionUuid);
        return ResponseEntity.ok(questionService.getQuestion(id));
    }
}
