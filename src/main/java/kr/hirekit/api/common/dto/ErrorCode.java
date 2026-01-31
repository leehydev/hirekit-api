package kr.hirekit.api.common.dto;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * API 공통 에러 코드.
 * HTTP 상태와 함께 클라이언트에서 분기 처리할 수 있도록 code, message를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SUCCESS("SUCCESS", "성공", HttpStatus.OK),

    // 4xx
    BAD_REQUEST("BAD_REQUEST", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("VALIDATION_ERROR", "입력값 검증에 실패했습니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CONFLICT("CONFLICT", "이미 존재하는 데이터입니다.", HttpStatus.CONFLICT),
    COMPANY_NOT_FOUND("COMPANY_NOT_FOUND", "기업을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    QUESTION_NOT_FOUND("QUESTION_NOT_FOUND", "질문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ANSWER_NOT_FOUND("ANSWER_NOT_FOUND", "답변을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    // 5xx
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
