package org.swyp.dessertbee.statistics.common.dto.user;

import lombok.Getter;

@Getter
public class UserCountResponseDto {
    private final long userCount;

    public UserCountResponseDto(long userCount) {
        this.userCount = userCount;
    }
}