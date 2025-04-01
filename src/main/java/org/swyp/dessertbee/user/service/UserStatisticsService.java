package org.swyp.dessertbee.user.service;

import org.swyp.dessertbee.user.dto.response.UserCountResponseDto;
import org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto;

import java.util.List;

public interface UserStatisticsService {

//    List<UserStatisticsResponseDto> getAllUsers();

    UserCountResponseDto getTotalUserCount();
    UserCountResponseDto getTotalUserOwnersCount();
}
