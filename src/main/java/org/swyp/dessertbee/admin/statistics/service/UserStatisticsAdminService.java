package org.swyp.dessertbee.admin.statistics.service;

import org.swyp.dessertbee.user.dto.response.UserCountResponseDto;
import org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface UserStatisticsAdminService {

//    List<UserStatisticsResponseDto> getAllUsers();

    UserCountResponseDto getTotalUserCount();

    UserCountResponseDto getTotalUserOwnersCount();

    UserCountResponseDto getNewUsersByDay(LocalDate date);
    UserCountResponseDto getNewUsersByWeek(int year, int week);
    UserCountResponseDto getNewUsersByMonth(int year, int month);
}
