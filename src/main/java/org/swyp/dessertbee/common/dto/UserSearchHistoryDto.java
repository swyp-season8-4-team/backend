package org.swyp.dessertbee.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.dessertbee.common.entity.UserSearchHistory;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserSearchHistoryDto {
    private Long id;
    private String keyword;
    private LocalDateTime createdAt;

    public static UserSearchHistoryDto fromEntity(UserSearchHistory entity) {
        return new UserSearchHistoryDto(entity.getId(), entity.getKeyword(), entity.getCreatedAt());
    }
}
