package org.swyp.dessertbee.common.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.dto.PopularSearchResponse;
import org.swyp.dessertbee.common.entity.PopularSearchKeyword;
import org.swyp.dessertbee.common.entity.UserSearchHistory;
import org.swyp.dessertbee.common.repository.PopularSearchKeywordRepository;
import org.swyp.dessertbee.common.repository.UserSearchHistoryRepository;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserSearchHistoryRepository searchHistoryRepository;
    private final PopularSearchKeywordRepository popularSearchKeywordRepository;
    private final StringRedisTemplate redisTemplate;

    private static final int MAX_RECENT_SEARCHES = 10;
    private static final String POPULAR_SEARCH_KEY = "popular_search_keywords";
    private static final String SYNCED_POPULAR_SEARCH_KEY = "synced_popular_search_keywords"; // 동기화된 데이터 저장용
    private static final String POPULAR_SEARCH_UPDATE_TIME = "popular_search_update_time"; // 업데이트 시간

    public String removeTrailingSpaces(String input) {
        return input.replaceAll("\\s+$", ""); // 문자열 끝부분의 공백만 제거
    }

    /** 최근 검색어 저장 (인증된 사용자만) */
    @Transactional
    public void saveRecentSearch(Long userId, String keyword) {
        if (userId == null || keyword == null || keyword.isEmpty()) {
            return;
        }

        // 기존 검색어 삭제 (중복 방지)
        searchHistoryRepository.deleteByUserIdAndKeyword(userId, keyword);

        // 새 검색어 저장
        searchHistoryRepository.save(UserSearchHistory.create(userId, keyword));

        // 검색어 최대 10개 유지
        deleteOldSearches(userId);
    }

    /** 검색어 10개 초과 시 예전 검색어 삭제 */
    @Transactional
    public void deleteOldSearches(Long userId) {
        Pageable pageable = PageRequest.of(0, MAX_RECENT_SEARCHES);
        List<Long> searchIds = searchHistoryRepository.findRecentSearchIdsByUserId(userId, pageable);

        if (!searchIds.isEmpty()) {
            searchHistoryRepository.deleteByUserIdAndIdNotIn(userId, searchIds);
        }
    }

    /** 최근 검색어 조회 */
    public List<String> getRecentSearches(Long userId) {
        if (userId == null) {
            return Collections.emptyList(); // 로그인되지 않은 경우 빈 리스트 반환
        }

        return searchHistoryRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(UserSearchHistory::getKeyword)
                .toList();
    }

    /**
     * 검색어 저장 (Redis)
     */
    public void savePopularSearch(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        keyword = keyword.trim();

        redisTemplate.opsForZSet().incrementScore(POPULAR_SEARCH_KEY, keyword, 1);
    }

    /**
     * 실시간 인기 검색어 조회 (이전 검색 횟수 차이 포함)
     */
    public Map<String, Object> getPopularSearchesWithDifference(int limit) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 현재 인기 검색어 순위 조회 (검색 횟수 포함)
        Set<ZSetOperations.TypedTuple<String>> currentTopSearches = zSetOps.reverseRangeWithScores(POPULAR_SEARCH_KEY, 0, limit - 1);
        Set<ZSetOperations.TypedTuple<String>> syncedTopSearches = zSetOps.reverseRangeWithScores(SYNCED_POPULAR_SEARCH_KEY, 0, limit - 1);

        log.info("현재 인기 검색어 데이터 (POPULAR_SEARCH_KEY): {}", currentTopSearches);
        log.info("이전 인기 검색어 데이터 (SYNCED_POPULAR_SEARCH_KEY): {}", syncedTopSearches);

        // 이전 인기 검색어 목록을 리스트로 변환하여 순위 매핑
        Map<String, Integer> previousRanks = new HashMap<>();
        if (syncedTopSearches != null) {
            List<String> previousRankList = syncedTopSearches.stream()
                    .map(ZSetOperations.TypedTuple::getValue)
                    .toList();
            for (int i = 0; i < previousRankList.size(); i++) {
                previousRanks.put(previousRankList.get(i), i + 1); // 1위부터 시작
            }
        }

        List<PopularSearchResponse> responseList = new ArrayList<>();
        if (currentTopSearches != null) {
            List<String> currentRankList = currentTopSearches.stream()
                    .map(ZSetOperations.TypedTuple::getValue)
                    .toList();

            for (int i = 0; i < currentRankList.size(); i++) {
                String keyword = currentRankList.get(i);
                int currentRank = i + 1; // 1위부터 시작
                int previousRank = previousRanks.getOrDefault(keyword, 0); // 이전 순위 (없으면 0)
                int difference = (previousRank == 0) ? 0 : previousRank - currentRank; // 순위 변동량

                // 현재 검색 횟수 가져오기
                Double score = zSetOps.score(POPULAR_SEARCH_KEY, keyword);
                int searchCount = (score != null) ? score.intValue() : 0;

                responseList.add(new PopularSearchResponse(keyword, searchCount, currentRank, difference));
            }
        }

        // Redis에서 마지막 업데이트 시간 가져오기
        String lastUpdatedTime = redisTemplate.opsForValue().get(POPULAR_SEARCH_UPDATE_TIME);
        if (lastUpdatedTime == null) {
            lastUpdatedTime = Instant.now().toString(); // 기본값 설정
        }

        Map<String, Object> response = new HashMap<>();
        response.put("lastUpdatedTime", lastUpdatedTime);
        response.put("searches", responseList);

        return response;
    }

    /**
     * 1분마다 MySQL에 Redis 데이터 동기화 (초기화 X)
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncPopularSearchesToDB() {
        log.info("실시간 인기 검색어 동기화 시작");

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> topKeywords = zSetOps.reverseRangeWithScores(POPULAR_SEARCH_KEY, 0, 49);

        if (topKeywords == null || topKeywords.isEmpty()) {
            log.info("Redis에 저장된 검색어 없음");
            return;
        }

        // 이전 동기화된 Redis 값 가져오기
        Set<ZSetOperations.TypedTuple<String>> syncedTopKeywords = zSetOps.reverseRangeWithScores(SYNCED_POPULAR_SEARCH_KEY, 0, 49);
        Map<String, Integer> syncedCounts = new HashMap<>();

        if (syncedTopKeywords != null) {
            for (ZSetOperations.TypedTuple<String> tuple : syncedTopKeywords) {
                syncedCounts.put(tuple.getValue(), tuple.getScore().intValue());
            }
        }

        for (ZSetOperations.TypedTuple<String> tuple : topKeywords) {
            String keyword = tuple.getValue();
            int redisCount = tuple.getScore().intValue();

            // MySQL에서 기존 searchCount 가져오기
            int dbCount = popularSearchKeywordRepository.findByKeyword(keyword)
                    .map(PopularSearchKeyword::getSearchCount)
                    .orElse(0);

            int increment = Math.max(0, redisCount - dbCount);

            if (increment > 0) {
                PopularSearchKeyword existingKeyword = popularSearchKeywordRepository.findByKeyword(keyword)
                        .orElse(PopularSearchKeyword.create(keyword));

                existingKeyword.incrementCount(increment);
                popularSearchKeywordRepository.save(existingKeyword);
            }

            // 현재 Redis 값 저장
            zSetOps.add(SYNCED_POPULAR_SEARCH_KEY, keyword, redisCount);
        }

        // 최신 업데이트 시간 저장
        redisTemplate.opsForValue().set(POPULAR_SEARCH_UPDATE_TIME, Instant.now().toString());

        log.info("✅ 실시간 인기 검색어 동기화 완료");
    }

    /**
     * 매일 자정 MySQL로 데이터 이전 후 Redis 초기화
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void midnightSyncPopularSearchesToDB() {
        log.info("인기 검색어 백업 및 초기화 시작");

        Set<String> keywords = redisTemplate.opsForZSet().reverseRange(POPULAR_SEARCH_KEY, 0, -1);
        if (keywords == null || keywords.isEmpty()) {
            log.info("백업할 인기 검색어 없음");
            return;
        }

        for (String keyword : keywords) {
            Double score = redisTemplate.opsForZSet().score(POPULAR_SEARCH_KEY, keyword);
            if (score != null) {
                PopularSearchKeyword existingKeyword = popularSearchKeywordRepository.findByKeyword(keyword)
                        .orElse(PopularSearchKeyword.create(keyword));

                existingKeyword.incrementCount(score.intValue());
                popularSearchKeywordRepository.save(existingKeyword);
            }
        }

        clearPopularSearchCache();

        log.info("인기 검색어 백업 및 초기화 완료");
    }

    /**
     * Redis 인기 검색어 데이터 초기화 (테스트용)
     */
    @Transactional
    public void clearPopularSearchCache() {
        log.info("🔥 Redis 인기 검색어 데이터 초기화 실행");

        redisTemplate.delete(POPULAR_SEARCH_KEY);
        redisTemplate.delete(SYNCED_POPULAR_SEARCH_KEY);
        redisTemplate.delete(POPULAR_SEARCH_UPDATE_TIME);

        log.info("✅ Redis 데이터가 성공적으로 초기화되었습니다.");
    }

}