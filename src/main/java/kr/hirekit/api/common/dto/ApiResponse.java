package kr.hirekit.api.common.dto;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 공통 응답 포맷.
 * 정상/에러 모두 동일한 구조로 응답합니다.
 *
 * @param <T> data 타입 (에러 시 null)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 요청 성공 여부
     */
    private boolean success;

    /**
     * 응답/에러 코드 (예: SUCCESS, VALIDATION_ERROR, NOT_FOUND)
     */
    private String code;

    /**
     * 사용자에게 보여줄 메시지
     */
    private String message;

    /**
     * 정상 응답 시 실제 데이터 (에러 시 null)
     */
    private T data;

    /**
     * 검증 실패 등 필드별 에러 목록 (해당 시에만 포함)
     */
    private List<FieldErrorDetail> errors;

    // --- 성공 응답 ---

    public static <T> ApiResponse<T> success(T data) {
        return success(ErrorCode.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                true,
                ErrorCode.SUCCESS.getCode(),
                message,
                data,
                null
        );
    }

    public static ApiResponse<Void> success() {
        return success(ErrorCode.SUCCESS.getMessage(), null);
    }

    // --- 에러 응답 ---

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return error(errorCode.getCode(), message, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, List<FieldErrorDetail> errors) {
        return error(errorCode.getCode(), errorCode.getMessage(), errors);
    }

    public static <T> ApiResponse<T> error(String code, String message, List<FieldErrorDetail> errors) {
        return new ApiResponse<>(
                false,
                code,
                message,
                null,
                errors != null ? errors : Collections.emptyList()
        );
    }
}
