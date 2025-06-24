package org.swyp.dessertbee.common.profiler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.store.store.dto.response.StoreMapResponse;
import org.swyp.dessertbee.store.store.service.StoreService;

import java.util.Arrays;
import java.util.List;

/**
 * 성능 개선 측정 도구
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PerformanceProfiler {

    private final StoreService storeService;

    /**
     * 성능 측정 테스트 메서드
     */
    public void measurePerformance() {
        List<Integer> storeCounts = Arrays.asList(10, 25, 50, 100);

        for (Integer count : storeCounts) {
            measureStoreQuery(count);
        }
    }

    private void measureStoreQuery(int expectedCount) {
        log.info("=== {}개 가게 조회 성능 테스트 시작 ===", expectedCount);

        long startTime = System.currentTimeMillis();

        // 서울 강남역 기준으로 반경 2km 내 가게 조회
        List<StoreMapResponse> stores = storeService.getStoresByLocation(37.4979, 127.0276, 2000.0);

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        log.info("성능 측정 결과:");
        log.info("- 실제 조회된 가게 수: {}개", stores.size());
        log.info("- 실행 시간: {}ms", executionTime);
        log.info("- 가게당 평균 시간: {}ms", stores.isEmpty() ? 0 : executionTime / stores.size());

        // 성능 목표 체크
        long targetTime = stores.size() * 10; // 가게당 10ms 목표
        if (executionTime <= targetTime) {
            log.info("✅ 성능 목표 달성! (목표: {}ms 이하)", targetTime);
        } else {
            log.warn("⚠️ 성능 목표 미달성 (목표: {}ms, 실제: {}ms)", targetTime, executionTime);
        }

        log.info("=== 테스트 완료 ===\n");
    }
}