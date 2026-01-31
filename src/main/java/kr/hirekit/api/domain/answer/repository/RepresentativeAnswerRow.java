package kr.hirekit.api.domain.answer.repository;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepresentativeAnswerRow {

    private final UUID questionId;
    private final UUID answerId;
    private final long likeCount;
}
