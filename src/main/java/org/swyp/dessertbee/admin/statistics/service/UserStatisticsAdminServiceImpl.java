package org.swyp.dessertbee.admin.statistics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.user.dto.response.UserCountResponseDto;
import org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto;
import org.swyp.dessertbee.user.service.UserStatisticsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStatisticsAdminServiceImpl implements UserStatisticsAdminService {

    private final UserStatisticsService userStatisticsService;

    /**
     * 전체 사용자 조회
     */
    @Transactional(readOnly = true)
    public List<UserStatisticsResponseDto> getAllUsers(){
        return userStatisticsService.getAllUsers();
    }

    /**
     * 전체 사용자 수 조회
     */
    @Transactional(readOnly = true)
    public UserCountResponseDto getTotalUserCount(){
        return userStatisticsService.getTotalUserCount();
    }
    /**
     * 전체 사장님 수 조회
     */
    @Transactional(readOnly = true)
    public UserCountResponseDto getTotalUserOwnersCount(){
        return userStatisticsService.getTotalUserOwnersCount();
    }
    /**
    * 신규 가입자 수 조회
    */
    /** 일 마다 */
    @Transactional(readOnly = true)
    public UserCountResponseDto getNewUsersByDay(int year, int month, int day) {
        return userStatisticsService.getNewUsersByDay(year, month, day);
    }
    /** 주 마다 */
    @Transactional(readOnly = true)
    public UserCountResponseDto getNewUsersByWeek(int year, int month, int week) {
        return userStatisticsService.getNewUsersByWeek(year, month, week);
    }
    /** 월 마다 */
    @Transactional(readOnly = true)
    public UserCountResponseDto getNewUsersByMonth(int year, int month) {
        return userStatisticsService.getNewUsersByMonth(year, month);
    }
}
