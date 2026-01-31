package kr.hirekit.api.common.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import kr.hirekit.api.auth.member.entity.MemberStatus;
import kr.hirekit.api.auth.member.entity.SocialProvider;
import kr.hirekit.api.common.dto.CodeEnum;
import kr.hirekit.api.common.dto.CodeGroupResponse;
import kr.hirekit.api.common.dto.CodeItem;
import kr.hirekit.api.domain.answer.entity.AnswerVisibility;
import kr.hirekit.api.domain.answer.entity.PassStatus;
import kr.hirekit.api.domain.question.entity.Job;
import kr.hirekit.api.domain.question.entity.QuestionVisibility;

/**
 * CodeEnum 구현 enum 목록을 프론트 조회용 형태로 제공
 */
@Service
public class CodesService {

    /**
     * 모든 코드 그룹 조회 (type + codes 배열)
     * 프론트에서 인증 없이 조회 가능
     */
    public List<CodeGroupResponse> getAllCodeGroups() {
        return List.of(
                toCodeGroupResponse(Job.class),
                toCodeGroupResponse(PassStatus.class),
                toCodeGroupResponse(AnswerVisibility.class),
                toCodeGroupResponse(QuestionVisibility.class),
                toCodeGroupResponse(SocialProvider.class),
                toCodeGroupResponse(MemberStatus.class));
    }

    private <E extends Enum<E> & CodeEnum> CodeGroupResponse toCodeGroupResponse(Class<E> enumClass) {
        String type = enumClass.getSimpleName();
        E[] constants = enumClass.getEnumConstants();
        List<CodeItem> codes = Arrays.stream(constants)
                .map(e -> new CodeItem(e.name(), e.getLabel()))
                .toList();
        return new CodeGroupResponse(type, codes);
    }
}
