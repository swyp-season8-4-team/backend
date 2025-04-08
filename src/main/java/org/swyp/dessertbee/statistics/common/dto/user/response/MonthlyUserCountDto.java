package org.swyp.dessertbee.statistics.common.dto.user.response;

import lombok.Getter;

@Getter
public class MonthlyUserCountDto {
    private int month;
    private long count;

    public MonthlyUserCountDto(int month, long count) {
        this.month = month;
        this.count = count;
    }
}
