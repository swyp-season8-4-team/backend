package org.swyp.dessertbee.statistics.user.dto.response;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class WeeklyUserCountDto {
    private int week;
    private LocalDate startDate;
    private LocalDate endDate;
    private long count;

    public WeeklyUserCountDto(int week, LocalDate startDate, LocalDate endDate, long count) {
        this.week = week;
        this.startDate = startDate;
        this.endDate = endDate;
        this.count = count;
    }
}
