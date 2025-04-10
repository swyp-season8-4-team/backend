package org.swyp.dessertbee.statistics.user.dto.response;

import lombok.Getter;

import java.util.UUID;

@Getter
public class UserStatisticsResponseDto {

    private final UUID userUuid;
    private final Long userId;
    private final Long roleId;

    public UserStatisticsResponseDto(UUID userUuid, Long userId, Long roleId) {
        this.userUuid = userUuid;
        this.userId = userId;
        this.roleId = roleId;
    }

}
