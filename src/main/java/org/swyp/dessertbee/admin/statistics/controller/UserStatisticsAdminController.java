package org.swyp.dessertbee.admin.statistics.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.admin.statistics.service.UserStatisticsAdminService;
import org.swyp.dessertbee.user.dto.response.*;

import java.util.List;

@RestController
@Tag(name = "AdminStatistics", description = "관리자용 통계 지표 관리 API")
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class UserStatisticsAdminController {

    private final UserStatisticsAdminService userStatisticsAdminService;

    /**
     * 전체 사용자 조회
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/all")
    public ResponseEntity<List<UserStatisticsResponseDto>> getAllUsers(){
        return ResponseEntity.ok( userStatisticsAdminService.getAllUsers());
    }

    /**
     * 전체 사용자 수 조회
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/count/all")
    public ResponseEntity<UserCountResponseDto> getTotalUserCount() {
        return ResponseEntity.ok(userStatisticsAdminService.getTotalUserCount());
    }

    /**
     * 전체 사장님 수 조회
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/count/all-owners")
    public ResponseEntity<UserCountResponseDto> getTotalOwnersCount() {
        return ResponseEntity.ok(userStatisticsAdminService.getTotalUserOwnersCount());
    }

    /**
     * 신규 가입자 수 조회
     */
    // 특정 날짜(일) 기준 신규 가입자 수 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/count/new/day")
    public ResponseEntity<List<DailyUserCountDto>> getNewUsersByDay(@RequestParam int year,
                                                                    @RequestParam int month){
        return ResponseEntity.ok(userStatisticsAdminService.getNewUsersByDay(year, month));
    }

    // 특정 주 기준 신규 가입자 수 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/count/new/week")
    public ResponseEntity<List<WeeklyUserCountDto>> getNewUsersByWeek(@RequestParam int year,
                                                                      @RequestParam int month){
        return ResponseEntity.ok(userStatisticsAdminService.getNewUsersByWeek(year, month));
    }

    // 특정 월 기준 신규 가입자 수 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/count/new/month")
    public ResponseEntity<List<MonthlyUserCountDto>> getNewUsersByMonth(@RequestParam int year) {
        return ResponseEntity.ok(userStatisticsAdminService.getNewUsersByMonth(year));
    }
}

