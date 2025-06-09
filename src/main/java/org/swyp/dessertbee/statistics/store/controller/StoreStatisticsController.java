package org.swyp.dessertbee.statistics.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.statistics.store.dto.response.StoreStatisticsPeriodResponse;
import org.swyp.dessertbee.statistics.store.dto.response.StoreStatisticsTrendResponse;
import org.swyp.dessertbee.statistics.store.entity.enums.PeriodType;
import org.swyp.dessertbee.statistics.store.service.StoreStatisticsService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeUuid}/statistics")
@Tag(name = "StoreStatistics", description = "가게 통계 조회 API")
public class StoreStatisticsController {

    private final StoreStatisticsService storeStatisticsService;

    @GetMapping
    @Operation(
            summary = "선택일 기준 통계 조회",
            description = """
            선택한 날짜를 기준으로 일간, 주간, 월간 통계를 조회합니다.  
            - `DAILY`: 선택한 날짜 하루 (00시~24시 기준)  
            - `WEEKLY`: 선택한 날짜가 속한 주 (월~일 기준)  
            - `MONTHLY`: 선택한 날짜가 속한 월 전체 (1일~말일 기준)  
            평균 평점은 리뷰가 존재할 경우에만 제공됩니다.
            """,
            parameters = {
                    @Parameter(name = "storeUuid", description = "가게 UUID", example = "550e8400-e29b-41d4-a716-446655440000"),
                    @Parameter(name = "period", description = "조회 기간 유형", example = "WEEKLY"),
                    @Parameter(name = "date", description = "기준 날짜 (yyyy-MM-dd)", example = "2025-05-13")
            }
    )
    public StoreStatisticsPeriodResponse getStoreStatistics(
            @PathVariable("storeUuid") UUID storeUuid,
            @RequestParam("period") PeriodType periodType,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate
    ) {
        return storeStatisticsService.getStoreStatistics(storeUuid, periodType, selectedDate);
    }

    @GetMapping("/trend")
    @Operation(
            summary = "선택일 기준 통계 추이 조회",
            description = """
        선택한 날짜를 기준으로 통계 추이 데이터를 조회합니다.  
        - `DAILY`: 2시간 단위 (00시~24시)  
        - `WEEKLY`: 요일 단위 (MON~SUN)  
        - `MONTHLY`: 날짜 단위 (1~31일)  
        평균 평점은 해당 구간에 리뷰가 존재할 경우에만 제공됩니다.
        """,
            parameters = {
                    @Parameter(name = "storeUuid", description = "가게 UUID", example = "550e8400-e29b-41d4-a716-446655440000"),
                    @Parameter(name = "period", description = "조회 기간 유형", example = "DAILY"),
                    @Parameter(name = "date", description = "기준 날짜 (yyyy-MM-dd)", example = "2025-06-09")
            }
    )
    public List<StoreStatisticsTrendResponse> getTrendStatistics(
            @PathVariable("storeUuid") UUID storeUuid,
            @RequestParam("period") PeriodType periodType,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate
    ) {
        return storeStatisticsService.getTrendStatistics(storeUuid, periodType, selectedDate);
    }
}