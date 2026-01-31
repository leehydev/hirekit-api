package kr.hirekit.api.common.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코드 그룹 응답 (type + codes 배열)
 * 프론트에서 코드/라벨 조회 시 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CodeGroupResponse {

    private String type;
    private List<CodeItem> codes;
}
