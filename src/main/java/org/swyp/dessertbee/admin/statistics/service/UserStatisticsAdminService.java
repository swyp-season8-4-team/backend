package org.swyp.dessertbee.admin.statistics.service;

import org.swyp.dessertbee.user.dto.response.UserCountResponseDto;
import org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto;

import java.util.List;

public interface UserStatisticsAdminService {

//    List<UserStatisticsResponseDto> getAllUsers();

    UserCountResponseDto getTotalUserCount();
}
