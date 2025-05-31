package org.swyp.dessertbee.statistics.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.statistics.store.dto.response.StoreStatisticsPeriodResponse;
import org.swyp.dessertbee.statistics.store.entity.StoreStatisticsHourly;
import org.swyp.dessertbee.statistics.store.entity.enums.PeriodType;
import org.swyp.dessertbee.statistics.store.repostiory.StoreStatisticsHourlyRepository;
import org.swyp.dessertbee.statistics.common.exception.StoreStatisticsLogExceptions.*;
import org.swyp.dessertbee.store.store.repository.StoreRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreStatisticsService {

    private final StoreStatisticsHourlyRepository storeStatisticsHourlyRepository;
    private final StoreRepository storeRepository;

    public StoreStatisticsPeriodResponse getStoreStatistics(UUID storeUuid, PeriodType periodType, LocalDate start, LocalDate end) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (periodType == PeriodType.CUSTOM && (start == null || end == null)) {
            throw new CustomPeriodStatisticsException("CUSTOM 기간을 조회하려면 start와 end를 지정해야 합니다.");
        }

        LocalDate today = LocalDate.now();
        LocalDate from = switch (periodType) {
            case WEEK -> today.minusDays(6);
            case MONTH -> today.minusWeeks(3);
            case CUSTOM -> start;
        };
        LocalDate to = (periodType == PeriodType.CUSTOM) ? end : today;

        List<StoreStatisticsHourly> stats = storeStatisticsHourlyRepository.findByStoreIdAndDateBetween(storeId, from, to);

        int views = 0, saves = 0, reviewStore = 0, reviewComm = 0, coupon = 0, mate = 0;
        BigDecimal ratingSum = BigDecimal.ZERO;
        int ratingCount = 0;

        for (StoreStatisticsHourly stat : stats) {
            views += stat.getViewCount();
            saves += stat.getSaveCount();
            reviewStore += stat.getReviewStoreCount();
            reviewComm += stat.getReviewCommCount();
            coupon += stat.getCouponUsedCount();
            mate += stat.getMateCount();

            if (stat.getAverageRating() != null) {
                ratingSum = ratingSum.add(stat.getAverageRating());
                ratingCount++;
            }
        }

        BigDecimal avgRating = (ratingCount == 0)
                ? BigDecimal.ZERO
                : ratingSum.divide(BigDecimal.valueOf(ratingCount), 2, RoundingMode.HALF_UP);

        return new StoreStatisticsPeriodResponse(
                views, saves, reviewStore, reviewComm, coupon, mate, avgRating
        );
    }
}