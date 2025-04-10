package org.swyp.dessertbee.statistics.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.util.YearWeek;
import org.swyp.dessertbee.statistics.user.dto.response.*;
import org.swyp.dessertbee.statistics.user.repository.UserStatisticsRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStatisticsAdminServiceImpl implements UserStatisticsAdminService {

    private final UserStatisticsRepository userStatisticsRepository;
    private final RedisTemplate redisTemplate;

    private static final int CURRENT_YEAR = LocalDate.now().getYear();
    private static final int MIN_YEAR = 1900;

    private void validateYear(int year) {
        if (year > CURRENT_YEAR || year < MIN_YEAR) {
            throw new BusinessException(ErrorCode.INVALID_YEAR);
        }
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new BusinessException(ErrorCode.INVALID_MONTH);
        }
    }

    /**
     * 전체 사용자 조회
     */
    @Transactional(readOnly = true)
    public List<UserStatisticsResponseDto> getAllUsers() {
        return userStatisticsRepository.findAllUsersWithRoles();
    }

    /**
     * 전체 사용자 수 조회
     */
    @Transactional(readOnly = true)
    public UserCountResponseDto getTotalUserCount() {
        long userCount = userStatisticsRepository.countUsers();
        return new UserCountResponseDto(userCount);
    }

    /**
     * 전체 사장님 수 조회
     */
    @Transactional(readOnly = true)
    public UserCountResponseDto getTotalUserOwnersCount() {
        long userCount = userStatisticsRepository.countOwners();
        return new UserCountResponseDto(userCount);
    }

    /**
     * 신규 가입자 수 조회
     */
    /** 일 단위 조회 (특정 연도, 특정 월, 특정 일) */
    @Transactional(readOnly = true)
    public List<DailyUserCountDto> getNewUsersByDay(int year, int month){
        //날짜 입력값 유효성 검사
        validateYear(year);
        validateMonth(month);

        List<DailyUserCountDto> result = new ArrayList<>();
        LocalDate startDate = LocalDate.of(year, month, 1);
        int daysInMonth = startDate.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            LocalDateTime startDateTime = date.atStartOfDay();
            LocalDateTime endDateTime = date.atTime(23, 59, 59);

            long count = userStatisticsRepository.countNewUsersByDay(startDateTime, endDateTime);
            result.add(new DailyUserCountDto(date, count)); // 날짜와 수 함께 반환
        }

        return result;
    }

    /** 주 단위 조회 (특정 연도, 특정 월, 특정 주) */
    @Transactional(readOnly = true)
    public List<WeeklyUserCountDto> getNewUsersByWeek(int year, int month) {
        //날짜 입력값 유효성 검사
        validateYear(year);
        validateMonth(month);

        List<WeeklyUserCountDto> result = new ArrayList<>();

        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth());

        int weekNumber = 1;

        // 첫 주: 1일부터 시작해서 그 주의 일요일까지
        LocalDate firstSunday = firstDayOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDateTime firstStart = firstDayOfMonth.atStartOfDay();
        LocalDateTime firstEnd = firstSunday.atTime(23, 59, 59);
        long firstWeekCount = userStatisticsRepository.countNewUsersByWeek(firstStart, firstEnd);
        result.add(new WeeklyUserCountDto(weekNumber++, firstDayOfMonth, firstSunday, firstWeekCount));

        //  이후 주: 월요일부터 일요일까지
        LocalDate weekStart = firstSunday.plusDays(1); // 다음 주 월요일
        while (!weekStart.isAfter(lastDayOfMonth)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(lastDayOfMonth)) {
                weekEnd = lastDayOfMonth;
            }

            LocalDateTime startDateTime = weekStart.atStartOfDay();
            LocalDateTime endDateTime = weekEnd.atTime(23, 59, 59);

            long count = userStatisticsRepository.countNewUsersByWeek(startDateTime, endDateTime);
            result.add(new WeeklyUserCountDto(weekNumber++, weekStart, weekEnd, count));

            weekStart = weekStart.plusWeeks(1);
        }

        return result;
    }
    /** 월 마다 */
    @Transactional(readOnly = true)
    public  List<MonthlyUserCountDto> getNewUsersByMonth(int year) {
        //날짜 입력값 유효성 검사
        validateYear(year);

        List<MonthlyUserCountDto> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = startDate.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

            long userCount = userStatisticsRepository.countNewUsersByMonth(startDateTime, endDateTime);
            result.add(new MonthlyUserCountDto(month, userCount));
        }

        return result;
    }

    /**
    활성 사용자 수 조회
    */
    //TODO : 만료시간 명시
    /** 사용자 활동 추적 - 로그인 시 */
    public void trackUserActivity(String userUuid) {
        LocalDate today = LocalDate.now();
        String week = YearWeek.from(today);
        YearMonth month = YearMonth.from(today);

        redisTemplate.opsForSet().add("active:daily:" + today, userUuid);
        redisTemplate.opsForSet().add("active:weekly:" + week, userUuid);
        redisTemplate.opsForSet().add("active:monthly:" + month, userUuid);
    }
    //TODO
    /** DAU : 일일 활성 사용자 수 */
    public long getDAU(String date) {
        return redisTemplate.opsForSet().size("active:daily:" + date);
    }
    //TODO
    /** WAU : 주간 활성 사용자 수 */
    public long getWAU(String week) {
        return redisTemplate.opsForSet().size("active:weekly:" + week);
    }
    //TODO
    /** MAU : 월간 활성 사용자 수 */
    public long getMAU(String month) {
        return redisTemplate.opsForSet().size("active:monthly:" + month);
    }
}
