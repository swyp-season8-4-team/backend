package org.swyp.dessertbee.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.role.repository.UserRoleRepository;
import org.swyp.dessertbee.user.dto.response.UserCountResponseDto;
import org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

//    /**
//     * 전체 사용자 조회
//     */
//    @Transactional(readOnly = true)
//    public List<UserStatisticsResponseDto> getAllUsers() {
//        return userRepository.findAllUsersWithRoles();
//    }

    /**
     * 전체 사용자 수 조회
     */
    @Transactional(readOnly = true)
    public UserCountResponseDto getTotalUserCount() {
        long userCount = userRoleRepository.countUsers();
        return new UserCountResponseDto(userCount);
    }

    /**
     * 전체 사장님 수 조회
     */
    @Transactional(readOnly = true)
    public UserCountResponseDto getTotalUserOwnersCount() {
        long userCount = userRoleRepository.countOwners();
        return new UserCountResponseDto(userCount);
    }

    /**
     * 신규 가입자 수 조회
     */
    /** 일 마다 */
    @Transactional(readOnly = true)
    public UserCountResponseDto getNewUsersByDay(LocalDate date) {
        long userCount = userRepository.countNewUsersByDay(date);
        return new UserCountResponseDto(userCount);
    }
    /** 주 마다 */
    @Transactional(readOnly = true)
    public UserCountResponseDto getNewUsersByWeek(int year, int week) {
        LocalDate weekStart = LocalDate.of(year, 1, 1).plusWeeks(week - 1).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        long userCount = userRepository.countNewUsersByWeek(weekStart, weekEnd);
        return new UserCountResponseDto(userCount);
    }
    /** 월 마다 */
    @Transactional(readOnly = true)
    public UserCountResponseDto getNewUsersByMonth(int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
        long userCount = userRepository.countNewUsersByMonth(monthStart, monthEnd);
        return new UserCountResponseDto(userCount);
    }
}
