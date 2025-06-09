package org.swyp.dessertbee.statistics.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.statistics.common.exception.StoreStatisticsLogExceptions.*;
import org.swyp.dessertbee.statistics.store.dto.response.StoreStatisticsPeriodResponse;
import org.swyp.dessertbee.statistics.store.dto.response.StoreStatisticsTrendResponse;
import org.swyp.dessertbee.statistics.store.entity.StoreStatisticsPeriodic;
import org.swyp.dessertbee.statistics.store.entity.StoreStatisticsTrend;
import org.swyp.dessertbee.statistics.store.entity.enums.PeriodType;
import org.swyp.dessertbee.statistics.store.repository.StoreStatisticsPeriodRepository;
import org.swyp.dessertbee.statistics.store.repository.StoreStatisticsTrendRepository;
import org.swyp.dessertbee.store.store.exception.StoreExceptions.*;
import org.swyp.dessertbee.store.store.repository.StoreRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreStatisticsService {

    private final StoreStatisticsPeriodRepository storeStatisticsPeriodicRepository;
    private final StoreRepository storeRepository;
    private final StoreStatisticsTrendRepository trendRepository;

    public StoreStatisticsPeriodResponse getStoreStatistics(UUID storeUuid, PeriodType periodType, LocalDate selectedDate) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) throw new InvalidStoreUuidException();

        LocalDate from, to;

        switch (periodType) {
            case DAILY -> {
                from = selectedDate;
                to = selectedDate;
            }
            case WEEKLY -> {
                from = selectedDate.with(DayOfWeek.MONDAY);
                to = from.plusDays(6);
            }
            case MONTHLY -> {
                from = selectedDate.withDayOfMonth(1);
                to = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth());
            }
            default -> throw new InvalidPeriodTypeException();
        }

        List<StoreStatisticsPeriodic> stats = storeStatisticsPeriodicRepository.findByStoreIdAndDateBetweenAndPeriodType(
                storeId, from, to, periodType
        );

        int view = 0, save = 0, reviewStore = 0, reviewComm = 0, coupon = 0, mate = 0;
        BigDecimal ratingSum = BigDecimal.ZERO;
        int ratingCount = 0;

        for (StoreStatisticsPeriodic stat : stats) {
            view += stat.getViewCount();
            save += stat.getSaveCount();
            reviewStore += stat.getReviewStoreCount();
            reviewComm += stat.getReviewCommCount();
            coupon += stat.getCouponUsedCount();
            mate += stat.getMateCount();

            if (stat.getAverageRating() != null) {
                ratingSum = ratingSum.add(stat.getAverageRating());
                ratingCount++;
            }
        }

        // 평균 평점 계산: 리뷰가 1개 이상일 때만
        BigDecimal avgRating = (ratingCount > 0)
                ? ratingSum.divide(BigDecimal.valueOf(ratingCount), 2, RoundingMode.HALF_UP)
                : null;

        int totalReview = reviewStore + reviewComm;

        return new StoreStatisticsPeriodResponse(view, save, reviewStore, reviewComm, totalReview, coupon, mate, avgRating);
    }

    public List<StoreStatisticsTrendResponse> getTrendStatistics(UUID storeUuid, PeriodType periodType, LocalDate selectedDate) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) throw new InvalidStoreUuidException();

        List<StoreStatisticsTrend> trends = switch (periodType) {
            case DAILY -> trendRepository.findByStoreIdAndDateAndPeriodType(storeId, selectedDate, PeriodType.DAILY)
                    .stream()
                    .sorted(Comparator.comparing(t -> Integer.parseInt(t.getDisplayKey())))
                    .toList();

            case WEEKLY -> {
                LocalDate monday = selectedDate.with(DayOfWeek.MONDAY);
                LocalDate sunday = monday.plusDays(6);
                yield trendRepository.findByStoreIdAndDateBetweenAndPeriodType(storeId, monday, sunday, PeriodType.WEEKLY)
                        .stream()
                        .sorted(Comparator.comparing(t -> DayOfWeek.valueOf(t.getDisplayKey())))
                        .toList();
            }

            case MONTHLY -> {
                LocalDate firstDay = selectedDate.withDayOfMonth(1);
                LocalDate lastDay = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth());
                yield trendRepository.findByStoreIdAndDateBetweenAndPeriodType(storeId, firstDay, lastDay, PeriodType.MONTHLY)
                        .stream()
                        .sorted(Comparator.comparing(t -> Integer.parseInt(t.getDisplayKey())))
                        .toList();
            }
        };

        return trends.stream()
                .map(trend -> StoreStatisticsTrendResponse.fromEntity(trend, periodType))
                .toList();
    }
}
