package org.swyp.dessertbee.admin.statistics.service;

import org.swyp.dessertbee.user.dto.response.*;

import java.util.List;

public interface UserStatisticsAdminService {

    List<UserStatisticsResponseDto> getAllUsers();

    UserCountResponseDto getTotalUserCount();

    UserCountResponseDto getTotalUserOwnersCount();

    List<DailyUserCountDto> getNewUsersByDay(int year, int month);
    List<WeeklyUserCountDto> getNewUsersByWeek(int year, int month);
    List<MonthlyUserCountDto> getNewUsersByMonth(int year);
}
