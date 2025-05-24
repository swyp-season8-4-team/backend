package org.swyp.dessertbee.statistics.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.statistics.store.dto.response.StoreStatisticsPeriodResponse;
import org.swyp.dessertbee.statistics.store.entity.enums.PeriodType;
import org.swyp.dessertbee.statistics.store.service.StoreStatisticsService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeUuid}/statistics")
@Tag(name = "StoreStatistics", description = "가게 통계 조회 API")
public class StoreStatisticsController {

    private final StoreStatisticsService storeStatisticsService;

    @GetMapping
    @Operation(summary = "기간별 통계 조회", description = "주간, 월간 또는 사용자 정의 기간에 대한 가게 통계를 조회합니다.")
    public StoreStatisticsPeriodResponse getStoreStatistics(
            @PathVariable("storeUuid")
            @Parameter(description = "가게 UUID", example = "101")
            UUID storeUuid,

            @RequestParam("period")
            @Parameter(description = "조회할 기간 유형 (WEEK, MONTH, CUSTOM)", example = "WEEK")
            PeriodType periodType,

            @RequestParam(value = "start", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "시작일 (CUSTOM일 때만 사용)", example = "2025-05-01")
            LocalDate start,

            @RequestParam(value = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "종료일 (CUSTOM일 때만 사용)", example = "2025-05-20")
            LocalDate end
    ) {
        return storeStatisticsService.getStoreStatistics(storeUuid, periodType, start, end);
    }
}