package org.swyp.dessertbee.statistics.common.dto.user;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DailyUserCountDto {

    private LocalDate date;
    private long count;

    public DailyUserCountDto(LocalDate date, long count) {
        this.date = date;
        this.count = count;
    }
}
