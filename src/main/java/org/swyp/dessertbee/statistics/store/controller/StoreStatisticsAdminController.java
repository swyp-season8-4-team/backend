package org.swyp.dessertbee.statistics.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.statistics.common.scheduler.StoreStatisticsScheduler;

@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
@Tag(name = "StoreStatisticsAdmin", description = "[로컬 테스트용] 통계 집계 수동 트리거 API")
public class StoreStatisticsAdminController {

    private final StoreStatisticsScheduler storeStatisticsScheduler;

    @PostMapping("/flush")
    @Operation(summary = "Redis 통계 수동 집계", description = "Redis에 누적된 통계를 즉시 MySQL에 반영합니다.")
    public String flushStatisticsManually() {
        storeStatisticsScheduler.aggregateHourlyStatistics();
        return "통계 집계 완료";
    }
}
