package org.swyp.dessertbee.statistics.common.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.statistics.common.exception.StoreStatisticsLogExceptions.*;
import org.swyp.dessertbee.statistics.store.entity.StoreStatisticsPeriodic;
import org.swyp.dessertbee.statistics.store.entity.StoreStatisticsTrend;
import org.swyp.dessertbee.statistics.store.entity.enums.PeriodType;
import org.swyp.dessertbee.statistics.store.repository.StoreStatisticsPeriodRepository;
import org.swyp.dessertbee.statistics.store.repository.StoreStatisticsTrendRepository;
import org.swyp.dessertbee.store.store.repository.StoreRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreStatisticsScheduler {

    private final StringRedisTemplate redisTemplate;
    private final StoreStatisticsPeriodRepository repository;
    private final StoreRepository storeRepository;
    private final StoreStatisticsTrendRepository trendRepository;

    /**
     * 매일 00:00 기준 전날 통계를 집계하여 DAILY / WEEKLY / MONTHLY 데이터로 저장
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void aggregatePeriodicStatistics() {
        aggregatePeriodicStatistics(LocalDate.now().minusDays(1), false);
    }

    @Transactional
    public void aggregatePeriodicStatistics(LocalDate baseDate, boolean force) {
        // 동일 날짜 중복 집계 여부 확인
        String redisKey = "stat:aggregated:" + baseDate;
        if (!force && Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            log.info("[통계 스케줄러] {} 이미 집계됨 - 중복 집계 방지", baseDate);
            return;
        }

        log.info("[통계 스케줄러] {} 기준 일간/주간/월간 통계 집계 시작", baseDate);

        Map<Long, AggregatedStat> storeMap = new HashMap<>();

        Set<String> keys = redisTemplate.keys("stat:*:*:" + baseDate + ":*:*");
        if (keys == null || keys.isEmpty()) {
            log.info("[통계 스케줄러] 수집된 통계 키 없음");
            return;
        }

        for (String key : keys) {
            try {
                String[] parts = key.split(":"); // stat:action:category:date:storeId:hour
                if (parts.length != 6) continue;

                String action = parts[1];
                String category = parts[2];
                LocalDate date = LocalDate.parse(parts[3]);
                Long storeId = Long.parseLong(parts[4]);
                int hour = Integer.parseInt(parts[5]);

                int delta = Integer.parseInt(Objects.requireNonNull(redisTemplate.opsForValue().get(key)));

                AggregatedStat stat = storeMap.computeIfAbsent(storeId, id -> new AggregatedStat());

                switch (action + ":" + category) {
                    case "view:store" -> stat.view += delta;
                    case "save:store" -> stat.save += delta;
                    case "review:store" -> stat.reviewStore += delta;
                    case "review:comm" -> stat.reviewComm += delta;
                    case "coupon:used" -> stat.coupon += delta;
                    case "mate:comm" -> stat.mate += delta;
                }

                // 추이 통계 저장
                saveTrendStat(storeId, date, hour, action, category, delta);

                redisTemplate.delete(key);
            } catch (Exception e) {
                log.error("[통계 스케줄러] 키 처리 중 예외 발생: {}", key, e);
            }
        }

        // 누적된 값으로 PERIODIC 통계 저장
        for (Map.Entry<Long, AggregatedStat> entry : storeMap.entrySet()) {
            Long storeId = entry.getKey();
            AggregatedStat stat = entry.getValue();

            BigDecimal averageRating = storeRepository.findAverageRatingByStoreId(storeId);
            BigDecimal rounded = (averageRating != null ? averageRating.setScale(2, RoundingMode.HALF_UP) : null);

            saveIfNotExists(storeId, baseDate, PeriodType.DAILY, stat, rounded);
            saveOrUpdate(storeId, baseDate.with(DayOfWeek.MONDAY), PeriodType.WEEKLY, stat, rounded);
            saveOrUpdate(storeId, baseDate.withDayOfMonth(1), PeriodType.MONTHLY, stat, rounded);
        }

        log.info("[통계 스케줄러] 통계 집계 완료 - 총 가게 수: {}", storeMap.size());

        // 집계 완료 마킹 (1일 유지)
        redisTemplate.opsForValue().set(redisKey, "done", Duration.ofDays(1));
    }

    private void saveIfNotExists(Long storeId, LocalDate date, PeriodType type, AggregatedStat stat, BigDecimal rating) {
        boolean exists = repository.findByStoreIdAndDateAndPeriodType(storeId, date, type).isPresent();
        if (exists) return;

        // 소수점 둘째자리까지 반올림
        BigDecimal roundedRating = rating != null ? rating.setScale(2, RoundingMode.HALF_UP) : null;

        StoreStatisticsPeriodic newStat = StoreStatisticsPeriodic.builder()
                .storeId(storeId)
                .date(date)
                .periodType(type)
                .viewCount(stat.view)
                .saveCount(stat.save)
                .reviewStoreCount(stat.reviewStore)
                .reviewCommCount(stat.reviewComm)
                .couponUsedCount(stat.coupon)
                .mateCount(stat.mate)
                .averageRating(roundedRating)
                .build();

        repository.save(newStat);
    }

    private void saveOrUpdate(Long storeId, LocalDate date, PeriodType type, AggregatedStat stat, BigDecimal rating) {
        StoreStatisticsPeriodic entity = repository.findByStoreIdAndDateAndPeriodType(storeId, date, type)
                .orElseGet(() -> StoreStatisticsPeriodic.builder()
                        .storeId(storeId)
                        .date(date)
                        .periodType(type)
                        .viewCount(0)
                        .saveCount(0)
                        .reviewStoreCount(0)
                        .reviewCommCount(0)
                        .couponUsedCount(0)
                        .mateCount(0)
                        .averageRating(null)
                        .build());

        entity.addViewCount(stat.view);
        entity.addSaveCount(stat.save);
        entity.addReviewStoreCount(stat.reviewStore);
        entity.addReviewCommCount(stat.reviewComm);
        entity.addCouponUsedCount(stat.coupon);
        entity.addMateCount(stat.mate);

        if (rating != null) {
            BigDecimal roundedRating = rating.setScale(2, RoundingMode.HALF_UP);
            entity.updateAverageRating(roundedRating);
        }

        repository.save(entity);
    }

    // 추이 통계 저장
    private void saveTrendStat(Long storeId, LocalDate date, int hour, String action, String category, int delta) {
        BigDecimal storeRating = storeRepository.findAverageRatingByStoreId(storeId);
        BigDecimal roundedRating = storeRating != null ? storeRating.setScale(2, RoundingMode.HALF_UP) : null;

        for (PeriodType type : List.of(PeriodType.DAILY, PeriodType.WEEKLY, PeriodType.MONTHLY)) {
            String displayKey = switch (type) {
                case DAILY -> String.valueOf(hour / 2); // 0 ~ 11
                case WEEKLY -> date.getDayOfWeek().toString(); // MON ~ SUN
                case MONTHLY -> String.valueOf(date.getDayOfMonth()); // 1 ~ 31
                default -> throw new InvalidPeriodTypeException();
            };

            // 필드 매핑
            StoreStatisticsTrend trend = StoreStatisticsTrend.builder()
                    .storeId(storeId)
                    .date(date)
                    .periodType(type)
                    .displayKey(displayKey)
                    .viewCount(action.equals("view") ? delta : 0)
                    .saveCount(action.equals("save") ? delta : 0)
                    .reviewStoreCount(action.equals("review") && category.equals("store") ? delta : 0)
                    .reviewCommCount(action.equals("review") && category.equals("comm") ? delta : 0)
                    .couponUsedCount(action.equals("coupon") && category.equals("used") ? delta : 0)
                    .mateCount(action.equals("mate") && category.equals("comm") ? delta : 0)
                    .averageRating(roundedRating)
                    .build();

            trendRepository.save(trend);
        }
    }

    private static class AggregatedStat {
        int view, save, reviewStore, reviewComm, coupon, mate;
    }
}