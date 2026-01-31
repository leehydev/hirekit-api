package kr.hirekit.api.common.exception;

import kr.hirekit.api.common.dto.ErrorCode;
import lombok.Getter;

/**
 * 비즈니스 로직 예외.
 * 서비스에서 throw 시 GlobalExceptionHandler가 공통 에러 포맷으로 변환합니다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String message;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
}
