package kr.hirekit.api.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 필드 검증 실패 시 필드별 에러 정보.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FieldErrorDetail {

    private String field;
    private String message;

    public static FieldErrorDetail of(String field, String message) {
        return new FieldErrorDetail(field, message);
    }
}
