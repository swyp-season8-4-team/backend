package org.swyp.dessertbee.statistics.user.service;

import org.swyp.dessertbee.statistics.common.dto.user.*;

import java.util.List;

public interface UserStatisticsAdminService {

    List<UserStatisticsResponseDto> getAllUsers();

    UserCountResponseDto getTotalUserCount();

    UserCountResponseDto getTotalUserOwnersCount();

    List<DailyUserCountDto> getNewUsersByDay(int year, int month);
    List<WeeklyUserCountDto> getNewUsersByWeek(int year, int month);
    List<MonthlyUserCountDto> getNewUsersByMonth(int year);

    void trackUserActivity(String userUuid);
    long getDAU(String date);
    long getWAU(String week);
    long getMAU(String month);
}
