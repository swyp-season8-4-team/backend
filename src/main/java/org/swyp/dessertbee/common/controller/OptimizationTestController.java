package org.swyp.dessertbee.common.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.common.profiler.PerformanceProfiler;
import org.swyp.dessertbee.store.review.repository.StoreReviewRepository;
import org.swyp.dessertbee.store.schedule.dto.HolidayResponse;
import org.swyp.dessertbee.store.schedule.dto.OperatingHourResponse;
import org.swyp.dessertbee.store.schedule.service.StoreScheduleService;
import org.swyp.dessertbee.store.store.dto.response.StoreMapResponse;
import org.swyp.dessertbee.store.store.handler.StoreImageHandler;
import org.swyp.dessertbee.store.store.service.StoreService;
import org.swyp.dessertbee.store.tag.service.StoreTagService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 성능 테스트
 */
@RestController
@RequestMapping("/api/test")
@Slf4j
@RequiredArgsConstructor
@Profile({"dev", "release"})
public class OptimizationTestController {

    private final StoreService storeService;

    private final StoreTagService storeTagService;

    private final StoreScheduleService storeScheduleService;

    private final StoreImageHandler storeImageHandler;

    private final StoreReviewRepository storeReviewRepository;

    private final PerformanceProfiler performanceProfiler;

    /**
     * 모든 배치 메서드 동작 확인
     */
    @GetMapping("/batch-methods")
    public ResponseEntity<Map<String, Object>> testBatchMethods() {
        Map<String, Object> result = new HashMap<>();
        List<Long> testIds = Arrays.asList(1L, 2L, 3L);

        try {
            // 1. 태그 배치 조회
            Map<Long, List<String>> tags = storeTagService.getTagNamesBatch(testIds);
            result.put("tags", tags);
            log.info("StoreTagService.getTagNamesBatch() - OK");

            // 2. 운영시간 배치 조회
            Map<Long, List<OperatingHourResponse>> hours = storeScheduleService.getOperatingHoursBatch(testIds);
            result.put("operatingHours", hours);
            log.info("StoreScheduleService.getOperatingHoursBatch() - OK");

            // 3. 휴무일 배치 조회
            Map<Long, List<HolidayResponse>> holidays = storeScheduleService.getHolidaysBatch(testIds);
            result.put("holidays", holidays);
            log.info("StoreScheduleService.getHolidaysBatch() - OK");

            // 4. 이미지 배치 조회
            Map<Long, List<String>> images = storeImageHandler.getStoreImageUrlsBatch(testIds);
            result.put("images", images);
            log.info("StoreImageHandler.getStoreImageUrlsBatch() - OK");

            // 5. 리뷰 개수 배치 조회
            Map<Long, Integer> counts = storeReviewRepository.getReviewCountsBatch(testIds);
            result.put("reviewCounts", counts);
            log.info("StoreReviewRepository.getReviewCountsBatch() - OK");

            result.put("status", "SUCCESS");
            result.put("message", "모든 배치 메서드가 정상 동작합니다!");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("배치 메서드 오류: ", e);
            result.put("status", "ERROR");
            result.put("message", "배치 메서드 오류: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());

            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 성능 테스트 실행
     */
    @GetMapping("/performance")
    public ResponseEntity<String> testPerformance() {
        try {
            long startTime = System.currentTimeMillis();

            // 서울 강남역 기준 100km 반경 가게 조회
            List<StoreMapResponse> stores = storeService.getStoresByLocation(37.4979, 127.0276, 100000.0);

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            String result = String.format(
                    "성능 테스트 결과:\n" +
                            "- 조회된 가게 수: %d개\n" +
                            "- 실행 시간: %dms\n" +
                            "- 가게당 평균 시간: %.1fms\n" +
                            "- 목표 달성: %s (목표: 1000ms 이하)\n" +
                            "- 이미지 로딩: %s",
                    stores.size(),
                    executionTime,
                    stores.isEmpty() ? 0 : (double) executionTime / stores.size(),
                    executionTime <= 1000 ? "성공" : "개선 필요",
                    "즉시 표시"
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("성능 테스트 오류: " + e.getMessage());
        }
    }

    /**
     * 상세 성능 프로파일링
     */
    @GetMapping("/profile")
    public ResponseEntity<String> runDetailedProfile() {
        try {
            performanceProfiler.measurePerformance();
            return ResponseEntity.ok("상세 성능 프로파일링 완료. 로그를 확인하세요.");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("프로파일링 오류: " + e.getMessage());
        }
    }
}