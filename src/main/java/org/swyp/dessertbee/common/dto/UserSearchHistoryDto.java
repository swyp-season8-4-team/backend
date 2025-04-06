package org.swyp.dessertbee.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.dessertbee.common.entity.UserSearchHistory;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserSearchHistoryDto {
    @NotNull
    @Schema(description = "검색 기록 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "검색어", example = "케이크")
    private String keyword;

    @NotNull
    @Schema(description = "검색어 기록 생성 시간", example = "2025-04-04T10:00:00")
    private LocalDateTime createdAt;

    public static UserSearchHistoryDto fromEntity(UserSearchHistory entity) {
        return new UserSearchHistoryDto(entity.getId(), entity.getKeyword(), entity.getCreatedAt());
    }
}
