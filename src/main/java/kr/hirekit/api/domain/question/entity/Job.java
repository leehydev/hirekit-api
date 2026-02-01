package kr.hirekit.api.domain.question.entity;

import kr.hirekit.api.common.dto.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Job implements CodeEnum {

    BACKEND("백엔드"),
    FRONTEND("프론트엔드"),
    FULLSTACK("풀스택"),
    DEVOPS("DevOps"),
    DATA_ENGINEER("데이터 엔지니어"),
    ANDROID("Android"),
    IOS("iOS"),
    DESIGN_UI_UX("UI/UX 디자인"),
    DESIGN_GRAPHIC("그래픽 디자인"),
    DESIGN_PRODUCT("제품 디자인"),
    PLANNING("기획"),
    PM("PM"),
    DATA_ANALYSIS("데이터 분석"),
    MARKETING("마케팅"),
    OPERATIONS("운영"),
    CS("CS"),
    SALES("영업"),
    HR("인사"),
    FINANCE("재무/회계"),
    ETC("기타");

    private final String label;
}
