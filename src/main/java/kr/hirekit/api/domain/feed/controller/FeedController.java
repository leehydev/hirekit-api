package kr.hirekit.api.domain.feed.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hirekit.api.domain.feed.dto.CursorFeedResponse;
import kr.hirekit.api.domain.feed.service.FeedService;
import kr.hirekit.api.domain.question.entity.Job;
import lombok.RequiredArgsConstructor;

@Tag(name = "피드", description = "피드 조회 API (질문 + 대표 답변 1개, 권한별 답변 수)")
@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @Operation(summary = "피드 목록 조회 (커서 기반 무한스크롤)", description = "전체공개 질문만 시간 역순으로 조회. 각 질문당 대표 답변 1개(좋아요 많은 순→최신순). "
            + "비로그인 시 전체공개 답변만, 로그인 시 전체공개+회원공개 답변 포함. "
            + "answerCounts로 권한별 답변 수 제공 → UI에서 '로그인하고 N개 더 보기' 등 후킹 가능. "
            + "cursor: 첫 요청 시 생략, 이후 응답의 nextCursor 값을 그대로 전달.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<CursorFeedResponse> getFeed(
            @Parameter(description = "회사 ID 필터 (미입력 시 전체)") @RequestParam(name = "companyId", required = false) String companyUuid,
            @Parameter(description = "직무 필터 (Job enum 이름, 예: BACKEND, FRONTEND. 미입력 시 전체)") @RequestParam(name = "job", required = false) String jobParam,
            @Parameter(description = "다음 페이지 커서 (첫 요청 시 생략, 이전 응답의 nextCursor 전달)") @RequestParam(name = "cursor", required = false, defaultValue = "null") String cursor,
            @Parameter(description = "페이지 크기 (기본 , 최대 50)") @RequestParam(name = "size", required = false, defaultValue = "20") Integer size,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID memberId = userDetails != null ? UUID.fromString(userDetails.getUsername()) : null;
        UUID companyId = companyUuid != null ? UUID.fromString(companyUuid) : null;
        Job job = parseJob(jobParam);
        CursorFeedResponse feed = feedService.getFeed(companyId, memberId, job, cursor, size);
        return ResponseEntity.ok(feed);
    }

    private static Job parseJob(String jobParam) {
        if (jobParam == null || jobParam.isBlank()) {
            return null;
        }
        try {
            return Job.valueOf(jobParam.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
