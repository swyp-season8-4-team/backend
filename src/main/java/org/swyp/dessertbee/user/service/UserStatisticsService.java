package org.swyp.dessertbee.user.service;

import org.swyp.dessertbee.user.dto.response.UserCountResponseDto;
import org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface UserStatisticsService {

    List<UserStatisticsResponseDto> getAllUsers();

    UserCountResponseDto getTotalUserCount();
    UserCountResponseDto getTotalUserOwnersCount();
    UserCountResponseDto getNewUsersByDay(int year, int month, int day);
    UserCountResponseDto getNewUsersByWeek(int year, int month, int week);
    UserCountResponseDto getNewUsersByMonth(int year, int month);
}
