package kr.hirekit.api.domain.answer.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 답변 목록 커서: 마지막 답변의 (createdAt, id).
 * nextCursor 요청 시 "createdAt_uuid" 형식으로 전달.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AnswerCursor {

    private static final String SEP = "_";
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private LocalDateTime createdAt;
    private UUID answerId;

    public static String encode(LocalDateTime createdAt, UUID answerId) {
        return createdAt.format(ISO) + SEP + answerId;
    }

    /**
     * @return 파싱 성공 시 AnswerCursor, 실패 시 empty
     */
    public static Optional<AnswerCursor> parse(String cursor) {
        if (cursor == null || cursor.isBlank() || "null".equalsIgnoreCase(cursor)) {
            return Optional.empty();
        }
        int i = cursor.lastIndexOf(SEP);
        if (i <= 0 || i == cursor.length() - 1) {
            return Optional.empty();
        }
        try {
            LocalDateTime createdAt = LocalDateTime.parse(cursor.substring(0, i), ISO);
            UUID answerId = UUID.fromString(cursor.substring(i + 1));
            return Optional.of(new AnswerCursor(createdAt, answerId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
