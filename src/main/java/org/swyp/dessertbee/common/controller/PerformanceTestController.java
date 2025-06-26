package org.swyp.dessertbee.common.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.store.store.dto.response.StoreMapResponse;
import org.swyp.dessertbee.store.store.service.StoreService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/performance-test")
@Slf4j
@RequiredArgsConstructor
@Profile({"dev", "release"})
public class PerformanceTestController {

    private final StoreService storeService;

    /**
     * 단일 API 성능 테스트
     */
    @GetMapping("/single-test")
    public ResponseEntity<Map<String, Object>> singlePerformanceTest(
            @RequestParam(defaultValue = "37.5665") Double lat,
            @RequestParam(defaultValue = "126.9780") Double lng,
            @RequestParam(defaultValue = "100000") Double radius) {

        Map<String, Object> result = new HashMap<>();

        // 워밍업 (JVM 최적화)
        for (int i = 0; i < 3; i++) {
            storeService.getStoresByLocation(lat, lng, radius);
        }

        // 실제 성능 측정
        long startTime = System.currentTimeMillis();
        List<StoreMapResponse> stores = storeService.getStoresByLocation(lat, lng, radius);
        long endTime = System.currentTimeMillis();

        long executionTime = endTime - startTime;

        result.put("storeCount", stores.size());
        result.put("executionTimeMs", executionTime);
        result.put("avgTimePerStore", stores.isEmpty() ? 0 : (double) executionTime / stores.size());
        result.put("targetAchieved", executionTime <= 1000);
        result.put("performance", executionTime <= 500 ? "EXCELLENT" :
                executionTime <= 1000 ? "GOOD" :
                        executionTime <= 2000 ? "ACCEPTABLE" : "POOR");

        log.info("🔍 성능 테스트 결과: {}개 가게, {}ms 소요", stores.size(), executionTime);

        return ResponseEntity.ok(result);
    }

    /**
     * 다중 반복 성능 테스트 (통계적 신뢰성)
     */
    @GetMapping("/multiple-test")
    public ResponseEntity<Map<String, Object>> multiplePerformanceTest(
            @RequestParam(defaultValue = "10") int iterations,
            @RequestParam(defaultValue = "37.5665") Double lat,
            @RequestParam(defaultValue = "126.9780") Double lng,
            @RequestParam(defaultValue = "100000") Double radius) {

        List<Long> executionTimes = new ArrayList<>();
        int totalStores = 0;

        // 워밍업
        for (int i = 0; i < 3; i++) {
            storeService.getStoresByLocation(lat, lng, radius);
        }

        // 여러 번 실행하여 통계 수집
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            List<StoreMapResponse> stores = storeService.getStoresByLocation(lat, lng, radius);
            long endTime = System.currentTimeMillis();

            executionTimes.add(endTime - startTime);
            totalStores = stores.size(); // 마지막 실행 결과

            // 테스트 간 간격
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 통계 계산
        double avgTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long minTime = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0L);
        long maxTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0L);

        Map<String, Object> result = new HashMap<>();
        result.put("iterations", iterations);
        result.put("storeCount", totalStores);
        result.put("avgExecutionTimeMs", Math.round(avgTime));
        result.put("minExecutionTimeMs", minTime);
        result.put("maxExecutionTimeMs", maxTime);
        result.put("allExecutionTimes", executionTimes);
        result.put("consistentPerformance", (maxTime - minTime) <= (avgTime * 0.5)); // 편차가 평균의 50% 이하면 일관성 있음

        return ResponseEntity.ok(result);
    }

    /**
     * 모든 최적화된 메서드 성능 비교
     */
    @GetMapping("/all-methods")
    public ResponseEntity<Map<String, Object>> testAllOptimizedMethods(
            @RequestParam(defaultValue = "37.5665") Double lat,
            @RequestParam(defaultValue = "126.9780") Double lng,
            @RequestParam(defaultValue = "100000") Double radius) {

        Map<String, Object> results = new HashMap<>();

        try {
            // 🔧 모든 시간 변수를 미리 초기화
            long time1, time2, time3;
            int methodCount = 0;

            // 1. 기본 위치 기반 조회
            long start1 = System.currentTimeMillis();
            List<StoreMapResponse> stores1 = storeService.getStoresByLocation(lat, lng, radius);
            time1 = System.currentTimeMillis() - start1;
            methodCount++;

            results.put("getStoresByLocation", Map.of(
                    "executionTimeMs", time1,
                    "storeCount", stores1.size(),
                    "avgTimePerStore", stores1.isEmpty() ? 0 : (double) time1 / stores1.size()
            ));

            // 2. 키워드 검색 (항상 실행하도록 수정)
            long start2 = System.currentTimeMillis();
            List<StoreMapResponse> stores2 = storeService.getStoresByLocationAndKeyword(lat, lng, radius, "카페");
            time2 = System.currentTimeMillis() - start2;
            methodCount++;

            results.put("getStoresByLocationAndKeyword", Map.of(
                    "executionTimeMs", time2,
                    "storeCount", stores2.size(),
                    "avgTimePerStore", stores2.isEmpty() ? 0 : (double) time2 / stores2.size()
            ));

            // 3. 태그 기반 조회 (테스트 태그 ID 사용)
            List<Long> testTagIds = Arrays.asList(1L, 2L, 3L);
            long start3 = System.currentTimeMillis();
            List<StoreMapResponse> stores3 = storeService.getStoresByLocationAndTags(lat, lng, radius, testTagIds);
            time3 = System.currentTimeMillis() - start3;
            methodCount++;

            results.put("getStoresByLocationAndTags", Map.of(
                    "executionTimeMs", time3,
                    "storeCount", stores3.size(),
                    "avgTimePerStore", stores3.isEmpty() ? 0 : (double) time3 / stores3.size()
            ));

            // 전체 요약
            results.put("summary", Map.of(
                    "totalMethods", methodCount,
                    "allMethodsUnder1Second", time1 <= 1000 && time2 <= 1000 && time3 <= 1000,
                    "performanceGrade", calculateOverallGrade(time1, time2, time3),
                    "avgExecutionTime", (time1 + time2 + time3) / 3.0
            ));

        } catch (Exception e) {
            results.put("error", e.getMessage());
            log.error("성능 테스트 중 오류 발생", e);
        }

        return ResponseEntity.ok(results);
    }

    /**
     * 부하 테스트 (동시 요청)
     */
    @GetMapping("/load-test")
    public ResponseEntity<Map<String, Object>> loadTest(
            @RequestParam(defaultValue = "5") int concurrentUsers,
            @RequestParam(defaultValue = "10") int requestsPerUser) {

        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        long testStartTime = System.currentTimeMillis();

        try {
            // 동시 요청 실행
            for (int user = 0; user < concurrentUsers; user++) {
                for (int req = 0; req < requestsPerUser; req++) {
                    CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                        long start = System.currentTimeMillis();
                        storeService.getStoresByLocation(37.5665, 126.9780, 1000.0);
                        return System.currentTimeMillis() - start;
                    }, executor);
                    futures.add(future);
                }
            }

            // 모든 요청 완료 대기
            List<Long> executionTimes = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            long totalTestTime = System.currentTimeMillis() - testStartTime;

            // 통계 계산
            double avgTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
            long minTime = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0L);
            long maxTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0L);

            Map<String, Object> result = new HashMap<>();
            result.put("concurrentUsers", concurrentUsers);
            result.put("requestsPerUser", requestsPerUser);
            result.put("totalRequests", concurrentUsers * requestsPerUser);
            result.put("totalTestTimeMs", totalTestTime);
            result.put("avgResponseTimeMs", Math.round(avgTime));
            result.put("minResponseTimeMs", minTime);
            result.put("maxResponseTimeMs", maxTime);
            result.put("throughputRequestsPerSecond", (double) (concurrentUsers * requestsPerUser) / (totalTestTime / 1000.0));
            result.put("allResponseTimes", executionTimes);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("부하 테스트 중 오류 발생", e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "부하 테스트 실행 중 오류가 발생했습니다: " + e.getMessage());
            errorResult.put("errorType", e.getClass().getSimpleName());

            return ResponseEntity.badRequest().body(errorResult);

        } finally {
            // 예외 발생 여부와 관계없이 항상 안전하게 종료
            shutdownExecutorGracefully(executor);
        }
    }

    /**
     * ExecutorService Graceful Shutdown 유틸리티 메서드
     *
     * shutdown() → awaitTermination() → shutdownNow() → awaitTermination()
     *
     * @param executor 종료할 ExecutorService
     */
    private void shutdownExecutorGracefully(ExecutorService executor) {
        // 1단계: 정상 종료 시작 (새로운 작업 수락 중단, 진행 중인 작업은 완료 대기)
        executor.shutdown();

        try {
            // 2단계: 정상 종료 대기 (최대 60초)
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("ExecutorService가 60초 내에 정상 종료되지 않음. 강제 종료 시작...");

                // 3단계: 강제 종료 (진행 중인 작업 중단)
                List<Runnable> remainingTasks = executor.shutdownNow();

                if (!remainingTasks.isEmpty()) {
                    log.warn("{}개의 작업이 강제 종료됨", remainingTasks.size());
                }

                // 4단계: 강제 종료 완료 대기 (최대 30초)
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    log.error("ExecutorService 강제 종료 실패! 일부 스레드가 여전히 실행 중일 수 있습니다.");
                } else {
                    log.debug("ExecutorService 강제 종료 완료");
                }
            } else {
                log.debug("ExecutorService 정상 종료 완료");
            }

        } catch (InterruptedException e) {
            log.warn("ExecutorService 종료 대기 중 인터럽트 발생");

            // 현재 스레드의 인터럽트 상태 복원
            Thread.currentThread().interrupt();

            // 인터럽트 발생 시에도 강제 종료 시도
            List<Runnable> remainingTasks = executor.shutdownNow();
            if (!remainingTasks.isEmpty()) {
                log.warn("인터럽트로 인한 강제 종료: {}개 작업 취소됨", remainingTasks.size());
            }
        }
    }

    private String calculateOverallGrade(long time1, long time2, long time3) {
        double avgTime = (time1 + time2 + time3) / 3.0;
        if (avgTime <= 300) return "A+ (Excellent)";
        if (avgTime <= 500) return "A (Very Good)";
        if (avgTime <= 800) return "B (Good)";
        if (avgTime <= 1200) return "C (Acceptable)";
        return "D (Needs Improvement)";
    }
}