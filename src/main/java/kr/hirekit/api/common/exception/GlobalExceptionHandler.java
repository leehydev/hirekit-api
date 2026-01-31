package kr.hirekit.api.common.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import kr.hirekit.api.common.dto.ApiResponse;
import kr.hirekit.api.common.dto.ErrorCode;
import kr.hirekit.api.common.dto.FieldErrorDetail;
import lombok.extern.slf4j.Slf4j;

/**
 * 전역 예외 처리.
 * 모든 컨트롤러에서 발생한 예외를 공통 응답 포맷(ApiResponse)으로 변환합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation (@Valid) 실패 시
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        List<FieldErrorDetail> errors = e.getBindingResult().getFieldErrors().stream()
                .map(err -> FieldErrorDetail.of(err.getField(), err.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        ApiResponse<Void> response = ApiResponse.error(errorCode, errors);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /**
     * 비즈니스 예외 (NOT_FOUND, CONFLICT 등)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<Void> response = ApiResponse.error(errorCode, e.getMessage());
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /**
     * IllegalArgumentException (잘못된 인자 등)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;
        ApiResponse<Void> response = ApiResponse.error(errorCode, e.getMessage());
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /**
     * 그 외 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ApiResponse<Void> response = ApiResponse.error(errorCode);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }
}
