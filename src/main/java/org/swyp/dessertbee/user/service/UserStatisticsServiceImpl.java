package org.swyp.dessertbee.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.role.repository.UserRoleRepository;
import org.swyp.dessertbee.user.dto.response.UserCountResponseDto;
import org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.time.DayOfWeek;
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
    /** 일 단위 조회 (특정 연도, 특정 월, 특정 일) */
    @Transactional(readOnly = true)
    public UserCountResponseDto getNewUsersByDay(int year, int month, int day) {
        LocalDateTime startOfDay = LocalDate.of(year, month, day).atStartOfDay(); // 00:00:00
        LocalDateTime endOfDay = startOfDay.withHour(23).withMinute(59).withSecond(59); // 23:59:59

        long userCount = userRepository.countNewUsersByDay(startOfDay, endOfDay);
        return new UserCountResponseDto(userCount);
    }

    /** 주 단위 조회 (특정 연도, 특정 월, 특정 주) */
    @Transactional(readOnly = true)
    public UserCountResponseDto getNewUsersByWeek(int year, int month, int week) {
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate firstSunday = firstDayOfMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.SUNDAY));

        LocalDate weekStart = firstSunday.plusWeeks(week - 1);
        LocalDate weekEnd = weekStart.plusDays(6);

        LocalDateTime startDateTime = weekStart.atStartOfDay();  // 00:00:00 변환
        LocalDateTime endDateTime = weekEnd.atTime(23, 59, 59); // 23:59:59 변환

        long userCount = userRepository.countNewUsersByWeek(startDateTime, endDateTime);
        return new UserCountResponseDto(userCount);
    }
    /** 월 마다 */
    @Transactional(readOnly = true)
    public UserCountResponseDto getNewUsersByMonth(int year, int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDateTime startDateTime = monthStart.atStartOfDay();  // 00:00:00 변환
        LocalDateTime endDateTime = monthStart.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59); // 23:59:59 변환

        long userCount = userRepository.countNewUsersByMonth(startDateTime, endDateTime);
        return new UserCountResponseDto(userCount);
    }
}
