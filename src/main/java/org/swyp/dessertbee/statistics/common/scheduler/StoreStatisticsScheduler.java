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

    // 매일 새벽 5시 실행
    @Scheduled(cron = "0 0 5 * * *")
    public void aggregateHourlyStatistics() {
        log.info("[통계 스케줄러] Redis → MySQL 시간별 통계 집계 시작");

        // 1. Redis key 패턴 조회 (전날 포함, 예: stat:view:store:2025-05-24:100:13)
        Set<String> keys = redisTemplate.keys("stat:*:*:*:*:*");
        if (keys == null | keys.isEmpty()) {
            log.info("[통계 스케줄러] 수집할 키 없음");
            return;
        }

        for (String key : keys) {
            try {
                // 2. 키 파싱
                String[] parts = key.split(":");
                if (parts.length != 6) {
                    log.warn("[통계 스케줄러] 잘못된 키 형식: {}", key);
                    continue;
                }

                String action = parts[1];     // e.g. view
                String category = parts[2];   // e.g. store
                LocalDate date = LocalDate.parse(parts[3]);
                Long storeId = Long.valueOf(parts[4]);
                int hour = Integer.parseInt(parts[5]);

                // 3. Redis 값 조회
                String raw = redisTemplate.opsForValue().get(key);
                if (raw == null || raw.isBlank()) {
                    continue;
                }
                int delta = Integer.parseInt(raw);

                // 4. 기존 데이터 조회 또는 생성
                StoreStatisticsHourly stat = repository
                        .findByStoreIdAndDateAndHour(storeId, date, hour)
                        .orElseGet(() -> StoreStatisticsHourly.builder()
                                .storeId(storeId)
                                .date(date)
                                .hour(hour)
                                .build());

                // 5. 액션/카테고리별 필드 증가
                switch (action + ":" + category) {
                    case "view:store" -> stat.addViewCount(delta);
                    case "save:store" -> stat.addSaveCount(delta);
                    case "review:store" -> stat.addReviewStoreCount(delta);
                    case "review:comm" -> stat.addReviewCommCount(delta);
                    case "mate:comm" -> stat.addMateCount(delta);
                    case "coupon:used" -> stat.addCouponUsedCount(delta);
                    default -> {
                        log.warn("[통계 스케줄러] 알 수 없는 키 조합: {}", key);
                        continue;
                    }
                }

                // 6. 저장
                repository.save(stat);

                // 7. Redis 키 삭제 or TTL에 맡김 (여기선 삭제)
                redisTemplate.delete(key);

            } catch (Exception e) {
                log.error("[통계 스케줄러] 처리 실패 - key: {}", key, e);
            }
        }

        log.info("[통계 스케줄러] 통계 집계 완료. 총 처리 키 수: {}", keys.size());
    }
}