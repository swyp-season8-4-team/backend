package org.swyp.dessertbee.admin.statistics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.util.YearWeek;
import org.swyp.dessertbee.user.dto.response.*;
import org.swyp.dessertbee.user.service.UserStatisticsService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserStatisticsAdminServiceImpl implements UserStatisticsAdminService {

    private final UserStatisticsService userStatisticsService;
    private final RedisTemplate<String, String> redisTemplate;

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
    public List<DailyUserCountDto> getNewUsersByDay(int year, int month) {
        return userStatisticsService.getNewUsersByDay(year, month);
    }
    /** 주 마다 */
    @Transactional(readOnly = true)
    public List<WeeklyUserCountDto> getNewUsersByWeek(int year, int month){
        return userStatisticsService.getNewUsersByWeek(year, month);
    }
    /** 월 마다 */
    @Transactional(readOnly = true)
    public List<MonthlyUserCountDto> getNewUsersByMonth(int year) {
        return userStatisticsService.getNewUsersByMonth(year);
    }

    /**
    활성 사용자 수 조회
    */
    /** 사용자 활동 추적 - 로그인 시 */
    public void trackUserActivity(String userId) {
        LocalDate today = LocalDate.now();
        String week = YearWeek.from(today);
        YearMonth month = YearMonth.from(today);

        redisTemplate.opsForSet().add("active:daily:" + today, userId);
        redisTemplate.opsForSet().add("active:weekly:" + week, userId);
        redisTemplate.opsForSet().add("active:monthly:" + month, userId);

        // 자동 만료 설정 (데이터 정리용)
        redisTemplate.expire("active:daily:" + today, 40, TimeUnit.DAYS);
        redisTemplate.expire("active:weekly:" + week, 60, TimeUnit.DAYS);
        redisTemplate.expire("active:monthly:" + month, 120, TimeUnit.DAYS);
    }
    /** DAU : 일일 활성 사용자 수 */
    public long getDAU(String date) {
        return redisTemplate.opsForSet().size("active:daily:" + date);
    }
    /** WAU : 주간 활성 사용자 수 */
    public long getWAU(String week) {
        return redisTemplate.opsForSet().size("active:weekly:" + week);
    }

    /** MAU : 월간 활성 사용자 수 */
    public long getMAU(String month) {
        return redisTemplate.opsForSet().size("active:monthly:" + month);
    }
}
