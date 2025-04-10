package org.swyp.dessertbee.statistics.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.statistics.common.dto.user.response.*;
import org.swyp.dessertbee.statistics.user.service.UserStatisticsAdminService;

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
    @Operation(summary = "전체 사용자 수 조회")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/count/all")
    public ResponseEntity<UserCountResponseDto> getTotalUserCount() {
        return ResponseEntity.ok(userStatisticsAdminService.getTotalUserCount());
    }

    /**
     * 전체 사장님 수 조회
     */
    @Operation(summary = "전체 사장님 수 조회")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/count/all-owners")
    public ResponseEntity<UserCountResponseDto> getTotalOwnersCount() {
        return ResponseEntity.ok(userStatisticsAdminService.getTotalUserOwnersCount());
    }

    /**
     * 신규 가입자 수 조회
     */
    // 특정 날짜(일) 기준 신규 가입자 수 조회
    @Operation(summary = "신규 가입자 수 조회(일)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/count/new/day")
    public ResponseEntity<List<DailyUserCountDto>> getNewUsersByDay(@RequestParam int year,
                                                                    @RequestParam int month){
        return ResponseEntity.ok(userStatisticsAdminService.getNewUsersByDay(year, month));
    }

    // 특정 주 기준 신규 가입자 수 조회
    @Operation(summary = "신규 가입자 수 조회(주)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/count/new/week")
    public ResponseEntity<List<WeeklyUserCountDto>> getNewUsersByWeek(@RequestParam int year,
                                                                      @RequestParam int month){
        return ResponseEntity.ok(userStatisticsAdminService.getNewUsersByWeek(year, month));
    }

    // 특정 월 기준 신규 가입자 수 조회
    @Operation(summary = "신규 가입자 수 조회(월)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/count/new/month")
    public ResponseEntity<List<MonthlyUserCountDto>> getNewUsersByMonth(@RequestParam int year) {
        return ResponseEntity.ok(userStatisticsAdminService.getNewUsersByMonth(year));
    }

    /**
     * 활성 사용자 수 조회
     */
    // 활성 사용자 추가
    @Operation(summary = "활성 사용자 추가")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/trackUserActivity")
    public ResponseEntity<Void> track(@RequestParam String userUuid) {
        userStatisticsAdminService.trackUserActivity(userUuid);
        return ResponseEntity.ok().build();
    }
    // DAU : 일일 활성 사용자 수
    @Operation(summary = "DAU 수 조회")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dau")
    public ResponseEntity<Long> getDAU(@RequestParam String date) {
        return ResponseEntity.ok(userStatisticsAdminService.getDAU(date));
    }
    // WAU : 주간 활성 사용자 수
    @Operation(summary = "WAU 조회")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/wau")
    public ResponseEntity<Long> getWAU(@RequestParam String week) {
        return ResponseEntity.ok(userStatisticsAdminService.getWAU(week));
    }
    // MAU : 월간 활성 사용자 수
    @Operation(summary = "MAU 조회")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/mau")
    public ResponseEntity<Long> getMAU(@RequestParam String month) {
        return ResponseEntity.ok(userStatisticsAdminService.getMAU(month));
    }
}

