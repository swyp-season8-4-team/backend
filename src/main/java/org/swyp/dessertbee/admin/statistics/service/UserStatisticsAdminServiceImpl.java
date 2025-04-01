package org.swyp.dessertbee.admin.statistics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto;
import org.swyp.dessertbee.user.service.UserStatisticsService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStatisticsAdminServiceImpl implements UserStatisticsAdminService {

    private final UserStatisticsService userStatisticsService;

    /**
     * 전체 사용자 수 조회
     */
    @Transactional(readOnly = true)
    public List<UserStatisticsResponseDto> getAllUsers(){
        return userStatisticsService.getAllUsers();
    }
}
