package org.swyp.dessertbee.statistics.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.statistics.store.entity.StoreStatisticsHourly;
import org.swyp.dessertbee.statistics.store.repostiory.StoreStatisticsHourlyRepository;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreStatisticsScheduler {

    private final StringRedisTemplate redisTemplate;
    private final StoreStatisticsHourlyRepository repository;

    @Scheduled(cron = "0 0 5 * * *") // 매일 새벽 5시
    public void aggregateHourlyStatistics() {
        log.info("[통계 스케줄러] Redis → MySQL 시간별 통계 집계 시작");

        Set<String> keys = redisTemplate.keys("stat:*:*:*:*:*");
        if (keys == null || keys.isEmpty()) {
            log.info("[통계 스케줄러] 수집할 키 없음");
            return;
        }

        int successCount = 0;
        int failCount = 0;
        int skippedCount = 0;

        for (String key : keys) {
            try {
                String[] parts = key.split(":");
                if (parts.length != 6) {
                    log.warn("[통계 스케줄러] 잘못된 키 형식: {}", key);
                    skippedCount++;
                    continue;
                }

                String action = parts[1];
                String category = parts[2];
                LocalDate date;
                Long storeId;
                int hour;

                try {
                    date = LocalDate.parse(parts[3]);
                    storeId = Long.parseLong(parts[4]);
                    hour = Integer.parseInt(parts[5]);
                } catch (Exception e) {
                    log.warn("[통계 스케줄러] 키 파싱 실패: {}", key, e);
                    skippedCount++;
                    continue;
                }

                // 중복 집계 방지 (이미 저장된 경우 skip)
                boolean alreadyExists = repository.existsByStoreIdAndDateAndHour(storeId, date, hour);
                if (alreadyExists) {
                    log.info("[통계 스케줄러] 이미 집계된 항목 - SKIP: {}", key);
                    skippedCount++;
                    continue;
                }

                String raw = redisTemplate.opsForValue().get(key);
                if (raw == null || raw.isBlank()) {
                    skippedCount++;
                    continue;
                }

                int delta;
                try {
                    delta = Integer.parseInt(raw);
                } catch (NumberFormatException e) {
                    log.warn("[통계 스케줄러] Redis 값이 숫자가 아님 - key: {}, value: {}", key, raw);
                    skippedCount++;
                    continue;
                }

                StoreStatisticsHourly stat = StoreStatisticsHourly.builder()
                        .storeId(storeId)
                        .date(date)
                        .hour(hour)
                        .build();

                switch (action + ":" + category) {
                    case "view:store" -> stat.addViewCount(delta);
                    case "save:store" -> stat.addSaveCount(delta);
                    case "review:store" -> stat.addReviewStoreCount(delta);
                    case "review:comm" -> stat.addReviewCommCount(delta);
                    case "mate:comm" -> stat.addMateCount(delta);
                    case "coupon:used" -> stat.addCouponUsedCount(delta);
                    default -> {
                        log.warn("[통계 스케줄러] 알 수 없는 키 조합: {}", key);
                        skippedCount++;
                        continue;
                    }
                }

                repository.save(stat);

                // Redis 삭제 시도
                Boolean deleted = redisTemplate.delete(key);
                if (Boolean.FALSE.equals(deleted)) {
                    log.warn("[통계 스케줄러] Redis 키 삭제 실패 - 중복 집계 우려: {}", key);
                }

                successCount++;

            } catch (Exception e) {
                log.error("[통계 스케줄러] 처리 실패 - key: {}", key, e);
                failCount++;
            }
        }

        log.info("[통계 스케줄러] 집계 완료 - 성공: {}, 실패: {}, 스킵: {}", successCount, failCount, skippedCount);
    }
}
