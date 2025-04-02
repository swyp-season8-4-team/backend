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
import org.swyp.dessertbee.user.dto.response.UserCountResponseDto;
import org.swyp.dessertbee.user.dto.response.UserResponseDto;
import org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Tag(name = "AdminStatistics", description = "관리자용 통계 지표 관리 API")
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class UserStatisticsAdminController {

    private final UserStatisticsAdminService userStatisticsAdminService;

//    /**
//     * 전체 사용자 조회
//     */
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/users/all")
//    public ResponseEntity<List<UserStatisticsResponseDto>> getAllUsers(){
//        return ResponseEntity.ok( userStatisticsAdminService.getAllUsers());
//    }

    /**
     * 전체 사용자 수 조회
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/all")
    public ResponseEntity<UserCountResponseDto> getTotalUserCount() {
        return ResponseEntity.ok(userStatisticsAdminService.getTotalUserCount());
    }

    /**
     * 전체 사장님 수 조회
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/all-owners")
    public ResponseEntity<UserCountResponseDto> getTotalOwnersCount() {
        return ResponseEntity.ok(userStatisticsAdminService.getTotalUserOwnersCount());
    }

    /**
     * 신규 가입자 수 조회
     */
    // 특정 날짜(일) 기준 신규 가입자 수 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/new/day")
    public ResponseEntity<UserCountResponseDto> getNewUsersByDay(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return ResponseEntity.ok(userStatisticsAdminService.getNewUsersByDay(parsedDate));
    }

    // 특정 주 기준 신규 가입자 수 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/new/week")
    public ResponseEntity<UserCountResponseDto> getNewUsersByWeek(@RequestParam int year, @RequestParam int week) {
        return ResponseEntity.ok(userStatisticsAdminService.getNewUsersByWeek(year, week));
    }

    // 특정 월 기준 신규 가입자 수 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/new/month")
    public ResponseEntity<UserCountResponseDto> getNewUsersByMonth(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(userStatisticsAdminService.getNewUsersByMonth(year, month));
    }
}

