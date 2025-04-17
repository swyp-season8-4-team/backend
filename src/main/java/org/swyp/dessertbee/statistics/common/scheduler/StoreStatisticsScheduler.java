package org.swyp.dessertbee.statistics.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.statistics.store.entity.StoreStatistics;
import org.swyp.dessertbee.statistics.store.entity.enums.PeriodType;
import org.swyp.dessertbee.statistics.store.repostiory.StoreStatisticsRepository;
import org.swyp.dessertbee.statistics.store.repostiory.StoreStatisticsSummaryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 매일 자정에 실행되어 가게별 주간/월간 통계를 요약 저장
 * 기준일: 오늘 - 1일 (어제까지의 통계 데이터 집계)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreStatisticsScheduler {

    private final StoreStatisticsRepository statisticsRepository;
    private final StoreStatisticsSummaryRepository summaryRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    public void summarizeStatistics() {
        log.info("[통계 요약] 주간/월간 집계 시작");

        List<Long> storeIds = statisticsRepository.findAllStoreIds();

        for (Long storeId : storeIds) {
            LocalDate today = LocalDate.now();

            // 자정 스케줄러 실행 시 오늘 데이터는 없을 것이기 때문에 어제까지를 기준으로 데이터 집계
            LocalDate targetDate = today.minusDays(1);

            // 주간
            summarizePeriod(storeId, targetDate.minusDays(6), targetDate, PeriodType.WEEK);
            // 월간
            summarizePeriod(storeId, targetDate.minusDays(29), targetDate, PeriodType.MONTH);
        }

        log.info("[통계 요약] 집계 완료");
    }

    private void summarizePeriod(Long storeId, LocalDate startDate, LocalDate endDate, PeriodType periodType) {
        List<StoreStatistics> stats = statisticsRepository
                .findByStoreIdAndDeletedAtIsNullAndStatDateBetweenOrderByStatDateAsc(storeId, startDate, endDate);

        if (stats.isEmpty()) return;

        summaryRepository.upsertSummary(
                storeId,
                periodType.name(),
                startDate,
                endDate,
                stats.stream().mapToInt(StoreStatistics::getViews).sum(),
                stats.stream().mapToInt(StoreStatistics::getSaves).sum(),
                stats.stream().mapToInt(StoreStatistics::getStoreReviewCount).sum(),
                stats.stream().mapToInt(StoreStatistics::getCommunityReviewCount).sum(),
                stats.stream().mapToInt(StoreStatistics::getDessertMateCount).sum(),
                stats.stream().mapToInt(StoreStatistics::getCouponUseCount).sum(),
                stats.stream()
                        .map(StoreStatistics::getAverageRating)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(new BigDecimal(stats.size()), 1, RoundingMode.HALF_UP)
        );
    }
}