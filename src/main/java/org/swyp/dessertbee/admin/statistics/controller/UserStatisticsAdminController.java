package org.swyp.dessertbee.admin.statistics.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.admin.statistics.service.UserStatisticsAdminService;
import org.swyp.dessertbee.user.dto.response.UserStatisticsResponseDto;

import java.util.List;

@RestController
@Tag(name = "AdminStatistics", description = "관리자용 통계 지표 관리 API")
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class UserStatisticsAdminController {

    private final UserStatisticsAdminService userStatisticsAdminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/all")
    public ResponseEntity<List<UserStatisticsResponseDto>> getAllUsers(){
        return ResponseEntity.ok( userStatisticsAdminService.getAllUsers());
    }
}
