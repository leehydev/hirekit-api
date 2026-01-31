package kr.hirekit.api.domain.answer.controller;

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
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hirekit.api.domain.answer.dto.AnswerCreateRequest;
import kr.hirekit.api.domain.answer.dto.AnswerDetailResponse;
import kr.hirekit.api.domain.answer.dto.AnswerLikeToggleResponse;
import kr.hirekit.api.domain.answer.dto.AnswerUpdateRequest;
import kr.hirekit.api.domain.answer.dto.AnswerVisibilityUpdateRequest;
import kr.hirekit.api.domain.answer.service.AnswerService;
import lombok.RequiredArgsConstructor;

@Tag(name = "답변", description = "답변 등록·조회·수정·삭제·공개상태 변경 및 좋아요 API")
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

    @Operation(summary = "답변 단건 조회", description = "질문이 전체공개이며, 답변 공개범위가 요청자에게 허용된 경우에만 조회 가능. 응답에 작성자 ID(authorId) 포함.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "질문/답변을 찾을 수 없거나 열람 권한 없음")
    })
    @GetMapping("/{questionId}/answers/{answerId}")
    public ResponseEntity<AnswerDetailResponse> getAnswer(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable("questionId") UUID questionId,
            @Parameter(description = "답변 ID (UUID)", required = true) @PathVariable("answerId") UUID answerId,
            @AuthenticationPrincipal UUID memberId) {
        return ResponseEntity.ok(answerService.getAnswer(questionId, answerId, memberId));
    }

    @Operation(summary = "답변 수정", description = "작성자만 수정 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "작성자만 수정 가능"),
            @ApiResponse(responseCode = "404", description = "질문/답변을 찾을 수 없음")
    })
    @PutMapping("/{questionId}/answers/{answerId}")
    public ResponseEntity<AnswerDetailResponse> updateAnswer(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable("questionId") UUID questionId,
            @Parameter(description = "답변 ID (UUID)", required = true) @PathVariable("answerId") UUID answerId,
            @RequestBody @Valid AnswerUpdateRequest request,
            @AuthenticationPrincipal UUID memberId) {
        return ResponseEntity.ok(answerService.updateAnswer(questionId, answerId, request, memberId));
    }

    @Operation(summary = "답변 삭제", description = "작성자만 삭제 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "작성자만 삭제 가능"),
            @ApiResponse(responseCode = "404", description = "질문/답변을 찾을 수 없음")
    })
    @DeleteMapping("/{questionId}/answers/{answerId}")
    public ResponseEntity<Void> deleteAnswer(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable("questionId") UUID questionId,
            @Parameter(description = "답변 ID (UUID)", required = true) @PathVariable("answerId") UUID answerId,
            @AuthenticationPrincipal UUID memberId) {
        answerService.deleteAnswer(questionId, answerId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "답변 공개상태 변경", description = "작성자만 변경 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "작성자만 변경 가능"),
            @ApiResponse(responseCode = "404", description = "질문/답변을 찾을 수 없음")
    })
    @PatchMapping("/{questionId}/answers/{answerId}/visibility")
    public ResponseEntity<AnswerDetailResponse> updateAnswerVisibility(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable("questionId") UUID questionId,
            @Parameter(description = "답변 ID (UUID)", required = true) @PathVariable("answerId") UUID answerId,
            @RequestBody @Valid AnswerVisibilityUpdateRequest request,
            @AuthenticationPrincipal UUID memberId) {
        return ResponseEntity.ok(answerService.updateAnswerVisibility(questionId, answerId, request, memberId));
    }

    @Operation(summary = "답변 좋아요 토글", description = "로그인한 회원이 특정 답변에 좋아요를 누르거나 취소합니다. 이미 좋아요 상태면 취소, 아니면 추가됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "질문/답변/회원을 찾을 수 없음")
    })
    @PostMapping("/{questionId}/answers/{answerId}/like")
    public ResponseEntity<AnswerLikeToggleResponse> toggleLike(
            @Parameter(description = "질문 ID (UUID)", required = true) @PathVariable("questionId") UUID questionId,
            @Parameter(description = "답변 ID (UUID)", required = true) @PathVariable("answerId") UUID answerId,
            @AuthenticationPrincipal UUID memberId) {
        return ResponseEntity.ok(answerService.toggleLike(questionId, answerId, memberId));
    }
}
