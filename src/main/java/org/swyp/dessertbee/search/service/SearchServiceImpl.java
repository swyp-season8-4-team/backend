package org.swyp.dessertbee.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.search.dto.PopularSearchResponse;
import org.swyp.dessertbee.search.dto.PopularSearchesList;
import org.swyp.dessertbee.search.dto.UserSearchHistoryDto;
import org.swyp.dessertbee.search.exception.SearchExceptions.*;
import org.swyp.dessertbee.search.entity.PopularSearchKeyword;
import org.swyp.dessertbee.search.entity.UserSearchHistory;
import org.swyp.dessertbee.search.repository.PopularSearchKeywordRepository;
import org.swyp.dessertbee.search.repository.UserSearchHistoryRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SearchServiceImpl implements SearchService {

    private final UserSearchHistoryRepository searchHistoryRepository;
    private final PopularSearchKeywordRepository popularSearchKeywordRepository;
    private final StringRedisTemplate redisTemplate;

    private static final int MAX_RECENT_SEARCHES = 10;
    private static final String POPULAR_SEARCH_KEY = "popular_search_keywords";
    private static final String SYNCED_POPULAR_SEARCH_KEY = "synced_popular_search_keywords"; // 동기화된 데이터 저장용
    private static final String POPULAR_SEARCH_UPDATE_TIME = "popular_search_update_time"; // 업데이트 시간

    private static final String SYNC_LOCK_KEY = "sync_lock"; // 동기화 중 여부를 체크하는 Redis 키
    private static final long SYNC_LOCK_TIMEOUT = 60; // 락 유지 시간 (초)

    private boolean acquireLock(String key, long timeout) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "LOCKED", Duration.ofSeconds(timeout));
        return Boolean.TRUE.equals(success);
    }

    private void releaseLock(String key) {
        redisTemplate.delete(key);
    }

    /** 공백 제거 */
    @Override
    public String removeTrailingSpaces(String input) {
        return input.replaceAll("\\s+$", ""); // 문자열 끝부분의 공백만 제거
    }

    /** 최근 검색어 저장 (인증된 사용자만) */
    @Override
    public void saveRecentSearch(Long userId, String keyword) {
        try{
            if (userId == null || keyword == null || keyword.isEmpty()) {
                return;
            }

            // 기존 검색어 삭제 (중복 방지)
            searchHistoryRepository.deleteByUserIdAndKeyword(userId, keyword);

            // 새 검색어 저장
            searchHistoryRepository.save(UserSearchHistory.create(userId, keyword));

            // 검색어 최대 10개 유지
            deleteOldSearches(userId);
        } catch (RecentCreationFailedException e){
            log.warn("최근 검색어 저장 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("최근 검색어 저장 처리 중 오류 발생", e);
            throw new SearchServiceException("최근 검색어 저장 처리 중 오류가 발생했습니다.");
        }
    }

    /** 검색어 10개 초과 시 예전 검색어 삭제 */
    @Override
    public void deleteOldSearches(Long userId) {
        try{
            Pageable pageable = PageRequest.of(0, MAX_RECENT_SEARCHES);
            List<Long> searchIds = searchHistoryRepository.findRecentSearchIdsByUserId(userId, pageable);

            if (!searchIds.isEmpty()) {
                searchHistoryRepository.deleteByUserIdAndIdNotIn(userId, searchIds);
            }
        } catch (OldKeywordDeleteFailedException e){
            log.warn("오래된 검색 기록 삭제 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("오래된 검색 기록 삭제 처리 중 오류 발생", e);
            throw new SearchServiceException("오래된 검색 기록 삭제 처리 중 오류가 발생했습니다.");
        }
    }

    /** 최근 검색어 조회 */
    @Override
    public List<UserSearchHistoryDto> getRecentSearches(Long userId) {
        try{
            if (userId == null) {
                return Collections.emptyList(); // 로그인되지 않은 경우 빈 리스트 반환
            }

            return searchHistoryRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(UserSearchHistoryDto::fromEntity)
                    .toList();
        } catch (Exception e) {
            log.error("최근 검색어 조회 처리 중 오류 발생", e);
            throw new SearchServiceException("최근 검색어 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 최근 검색어 삭제 */
    @Override
    public boolean deleteRecentSearch(Long userId, Long searchId) {
        try{
            int deletedCount = searchHistoryRepository.deleteByUserIdAndId(userId, searchId);
            return deletedCount > 0;
        } catch (RecentDeleteFailedException e){
            log.warn("검색 기록 삭제 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("검색 기록 삭제 처리 중 오류 발생", e);
            throw new SearchServiceException("검색 기록 삭제 처리 중 오류가 발생했습니다.");
        }
    }

    /** 최근 검색어 전체 삭제 */
    @Override
    public void deleteAllRecentSearches(Long userId) {
        try{
            searchHistoryRepository.deleteByUserId(userId);
        } catch (RecentDeleteFailedException e){
            log.warn("전체 검색 기록 삭제 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("전체 검색 기록 삭제 처리 중 오류 발생", e);
            throw new SearchServiceException("전체 검색 기록 삭제 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 검색어 저장 (Redis)
     */
    @Override
    public void savePopularSearch(String keyword) {
        try{
            if (keyword == null || keyword.isBlank()) {
                return;
            }
            keyword = keyword.trim();

            // 동기화 중이면 검색어 저장하지 않음
            if (Boolean.TRUE.equals(redisTemplate.hasKey(SYNC_LOCK_KEY))) {
                log.info("⏳ 동기화 중이므로 검색어 저장이 제한됩니다.");
                return;
            }

            redisTemplate.opsForZSet().incrementScore(POPULAR_SEARCH_KEY, keyword, 1);
        } catch (PopularCreationFailedException e){
            log.warn("인기 검색어 저장 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("인기 검색어 저장 처리 중 오류 발생", e);
            throw new SearchServiceException("인기 검색어 저장 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 실시간 인기 검색어 조회 (이전 검색 횟수 차이 포함)
     */
    @Override
    public PopularSearchesList getPopularSearchesWithDifference(int limit) {
        try{
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
            String lastUpdatedTime = Optional.ofNullable(
                    redisTemplate.opsForValue().get(POPULAR_SEARCH_UPDATE_TIME)
            ).orElse(Instant.now().toString());

            return new PopularSearchesList(lastUpdatedTime, responseList);
        } catch (Exception e) {
            log.error("인기 검색어 기록 조회 처리 중 오류 발생", e);
            throw new SearchServiceException("인기 검색어 기록 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 1분마다 MySQL에 Redis 데이터 동기화 (초기화 X)
     */
    public void syncPopularSearchesToDB() {
        try{
            // 자정 동기화 중이라면 실행하지 않음
            if (Boolean.TRUE.equals(redisTemplate.hasKey(SYNC_LOCK_KEY))) {
                log.info("⏳ 자정 동기화가 진행 중이므로 1분 동기화를 중단합니다.");
                return;
            }

            // 현재 시간이 23:59 ~ 00:01 사이라면 동기화 중단
            int currentHour = Instant.now().atZone(ZoneId.of("Asia/Seoul")).getHour();
            int currentMinute = Instant.now().atZone(ZoneId.of("Asia/Seoul")).getMinute();

            if (currentHour == 23 && currentMinute >= 59 || currentHour == 0 && currentMinute < 1) {
                log.info("⏸️ 자정 동기화 시간(23:59 ~ 00:01)이므로 1분 동기화 중단");
                return;
            }

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
                Optional<PopularSearchKeyword> optionalKeyword = popularSearchKeywordRepository.findByKeyword(keyword);
                int dbCount = optionalKeyword.map(PopularSearchKeyword::getSearchCount).orElse(0);

                int increment = Math.max(0, redisCount - dbCount);

                if (increment > 0) {
                    PopularSearchKeyword existingKeyword = optionalKeyword.orElse(PopularSearchKeyword.create(keyword));
                    existingKeyword.incrementCount(increment);
                    popularSearchKeywordRepository.save(existingKeyword);
                }

                // 현재 Redis 값 저장
                zSetOps.add(SYNCED_POPULAR_SEARCH_KEY, keyword, redisCount);
            }

            // 최신 업데이트 시간 저장
            redisTemplate.opsForValue().set(POPULAR_SEARCH_UPDATE_TIME, Instant.now().toString());

            log.info("✅ 실시간 인기 검색어 동기화 완료");
        } catch (PopularSyncFailedException e){
            log.warn("인기 검색어 동기화 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("인기 검색어 동기화 처리 중 오류 발생", e);
            throw new SearchServiceException("인기 검색어 동기화 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 매일 자정 MySQL로 데이터 이전 후 Redis 초기화
     */
    public void midnightSyncPopularSearchesToDB() {
        log.info("[스케줄러 시작] 인기 검색어 백업 at {}", LocalDateTime.now());

        // 락 설정
        if (!acquireLock(SYNC_LOCK_KEY, SYNC_LOCK_TIMEOUT)) {
            log.info("이미 동기화가 진행 중이므로 실행하지 않습니다.");
            return;
        }

        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

            // 기존 동기화된 인기 검색어 데이터 백업
            Set<ZSetOperations.TypedTuple<String>> backupSyncedData = zSetOps.reverseRangeWithScores(SYNCED_POPULAR_SEARCH_KEY, 0, -1);

            Set<String> keywords = zSetOps.reverseRange(POPULAR_SEARCH_KEY, 0, -1);
            if (keywords == null || keywords.isEmpty()) {
                log.info("⏸️ 백업할 인기 검색어 없음");
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

            // 기존 동기화된 데이터 복원
            if (backupSyncedData != null) {
                for (ZSetOperations.TypedTuple<String> tuple : backupSyncedData) {
                    redisTemplate.opsForZSet().add(SYNCED_POPULAR_SEARCH_KEY, tuple.getValue(), tuple.getScore());
                }
            }

            // 동기화가 성공한 경우에만 Redis 초기화
            clearPopularSearchCache();

            log.info("[스케줄러 종료] 인기 검색어 백업 at {}", LocalDateTime.now());

        } catch (PopularInitFailedException e) {
            log.warn("[스케줄러 실패] 인기 검색어 초기화 실패 - {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생 - Redis 초기화 중단", e);
            throw new PopularInitFailedException("자정 Redis 초기화 중 예상치 못한 오류가 발생했습니다.");
        } finally {
            // 락 해제
            releaseLock(SYNC_LOCK_KEY);
        }
    }

    /**
     * Redis 인기 검색어 데이터 초기화 (테스트용)
     */
    @Override
    public void clearPopularSearchCache() {
        try{
            log.info("🔥 Redis 인기 검색어 데이터 초기화 실행");

            redisTemplate.delete(POPULAR_SEARCH_KEY);
            redisTemplate.delete(SYNCED_POPULAR_SEARCH_KEY);
            redisTemplate.delete(POPULAR_SEARCH_UPDATE_TIME);

            log.info("✅ Redis 데이터가 성공적으로 초기화되었습니다.");
        } catch (PopularInitFailedException e) {
            log.warn("인기 검색어 초기화 실패 - {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("인기 검색어 초기화 처리 중 오류 발생", e);
            throw new PopularInitFailedException("Redis 초기화 중 예상치 못한 오류가 발생했습니다.");
        }
    }

}