package kr.hirekit.api.common.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BaseResponse {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
