package kr.hirekit.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코드 한 건 (프론트 코드/라벨 조회용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CodeItem {

    private String code;
    private String label;
}
