package kr.hirekit.api.common.dto;

// 모든 코드성 Enum이 구현해야 하는 인터페이스
// 규칙을 강제하여 일관된 구조 유지
public interface CodeEnum {

    // Enum 이름 반환 (DB 저장값)
    // Enum에 기본 내장된 name() 메서드 활용
    String name();

    // 화면 표시용 라벨 반환
    String getLabel();
}
