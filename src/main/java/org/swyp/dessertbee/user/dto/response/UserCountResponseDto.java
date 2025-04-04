package org.swyp.dessertbee.user.dto.response;

import lombok.Getter;

@Getter
public class UserCountResponseDto {
    private final long userCount;

    public UserCountResponseDto(long userCount) {
        this.userCount = userCount;
    }
}