package kr.hirekit.api.domain.question.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hirekit.api.domain.answer.dto.CursorAnswerListResponse;
import kr.hirekit.api.domain.question.dto.MembersOnlyAnswerCountResponse;
import kr.hirekit.api.domain.question.dto.QuestionCreateRequest;
import kr.hirekit.api.domain.question.dto.QuestionDetailResponse;
import kr.hirekit.api.domain.question.dto.QuestionUpdateRequest;
import kr.hirekit.api.domain.question.dto.QuestionVisibilityUpdateRequest;
import kr.hirekit.api.domain.question.service.QuestionService;
import lombok.RequiredArgsConstructor;

@Tag(name = "질문", description = "질문 조회 및 등록 API")
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "질문 등록", description = "로그인한 회원이 새 질문을 등록합니다. 등록자는 해당 질문의 작성자로 저장됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "기업 또는 회원을 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<QuestionDetailResponse> createQuestion(
            @RequestBody @Valid QuestionCreateRequest request,
            @AuthenticationPrincipal UUID memberId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.createQuestion(request, memberId));
    }

    @Operation(summary = "질문 수정", description = "작성자만 수정 가능. 답변이 하나라도 달려 있으면 수정 불가(409).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "작성자만 수정 가능"),
            @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "답변이 달려 있어 수정 불가")
    })
    @PutMapping("/{id}")
    public ResponseEntity<QuestionDetailResponse> updateQuestion(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable("id") UUID id,
            @RequestBody @Valid QuestionUpdateRequest request,
            @AuthenticationPrincipal UUID memberId) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request, memberId));
    }

    @Operation(summary = "질문 삭제", description = "작성자만 삭제 가능. 답변이 하나라도 달려 있으면 삭제 불가(409).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "작성자만 삭제 가능"),
            @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "답변이 달려 있어 삭제 불가")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable("id") UUID id,
            @AuthenticationPrincipal UUID memberId) {
        questionService.deleteQuestion(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "질문 공개상태 변경", description = "작성자만 변경 가능. 답변이 하나라도 달려 있으면 변경 불가(409).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "작성자만 변경 가능"),
            @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "답변이 달려 있어 변경 불가")
    })
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<QuestionDetailResponse> updateQuestionVisibility(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable("id") UUID id,
            @RequestBody @Valid QuestionVisibilityUpdateRequest request,
            @AuthenticationPrincipal UUID memberId) {
        return ResponseEntity.ok(questionService.updateQuestionVisibility(id, request, memberId));
    }

    @Operation(summary = "질문 단건 조회", description = "전체공개이며 강제 비공개가 아닌 질문만 조회 가능. 그 외는 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음(비공개/강제비공개 포함)")
    })
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponse> getQuestion(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable("id") UUID id) {
        return ResponseEntity.ok(questionService.getQuestion(id));
    }

    @Operation(summary = "질문별 답변 목록 조회 (커서 기반)", description = "전체공개 질문의 답변만 조회. 비로그인 시 전체공개 답변만, 로그인 시 전체공개+회원공개 답변 포함. "
            + "cursor: 첫 요청 시 생략, 이후 응답의 nextCursor 전달. size: 기본 20, 최대 50.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음(비공개/강제비공개 포함)")
    })
    @GetMapping("/{id}/answers")
    public ResponseEntity<CursorAnswerListResponse> getAnswersByQuestionId(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable("id") UUID id,
            @Parameter(description = "다음 페이지 커서 (첫 요청 시 생략)") @RequestParam(name = "cursor", required = false) String cursor,
            @Parameter(description = "페이지 크기 (기본 20, 최대 50)") @RequestParam(name = "size", required = false, defaultValue = "20") Integer size,
            @AuthenticationPrincipal UUID memberId) {
        return ResponseEntity.ok(questionService.getAnswersByQuestionId(id, memberId, cursor, size));
    }

    @Operation(summary = "회원 전용 답변 수 조회", description = "특정 질문의 회원 전용(MEMBERS_ONLY) 답변 수. 회원이면 0, 비회원이면 회원 전용 답변 수. public API.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "질문을 찾을 수 없음(비공개/강제비공개 포함)")
    })
    @GetMapping("/{id}/answers/members-only-count")
    public ResponseEntity<MembersOnlyAnswerCountResponse> getMembersOnlyAnswerCount(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable("id") UUID id,
            @AuthenticationPrincipal UUID memberId) {
        return ResponseEntity.ok(questionService.getMembersOnlyAnswerCount(id, memberId));
    }
}
