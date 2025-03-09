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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserSearchHistoryRepository searchHistoryRepository;
    private final PopularSearchKeywordRepository popularSearchKeywordRepository;
    private final StringRedisTemplate redisTemplate;

    private static final int MAX_RECENT_SEARCHES = 10;
    private static final String POPULAR_SEARCH_KEY = "popular_search_keywords";

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


    /** 인기 검색어 저장 (모든 사용자) */
    public void savePopularSearch(String keyword) {
        if (keyword == null || keyword.isBlank()) { // 공백 체크
            return;
        }

        keyword = keyword.trim(); // 공백 제거
        redisTemplate.opsForZSet().incrementScore(POPULAR_SEARCH_KEY, keyword, 1);
    }


    /** 인기 검색어 가져오기 */
    public Set<String> getPopularSearches(int limit) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.reverseRange(POPULAR_SEARCH_KEY, 0, limit - 1); // 상위 limit개 인기 검색어 가져오기
    }

    public List<PopularSearchResponse> getPopularSearchesFromDB(int limit) {
        return popularSearchKeywordRepository.findTopKeywords(limit)
                .stream()
                .map(p -> new PopularSearchResponse(p.getKeyword(), p.getSearchCount()))
                .collect(Collectors.toList());
    }

    /** 인기 검색어 동기화 (Redis → MySQL) */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void syncPopularSearchesToDB() {
        log.info("인기 검색어 동기화 시작");

        Set<String> keywords = redisTemplate.opsForZSet().reverseRange(POPULAR_SEARCH_KEY, 0, 49);
        if (keywords == null || keywords.isEmpty()) {
            log.info("Redis에 인기 검색어 없음");
            return;
        }

        for (String keyword : keywords) {
            Double score = redisTemplate.opsForZSet().score(POPULAR_SEARCH_KEY, keyword);
            if (score != null) {
                log.info("MySQL 업데이트: " + keyword + " (" + score.intValue() + ")");

                // 조회 후 저장
                PopularSearchKeyword existingKeyword = popularSearchKeywordRepository.findByKeyword(keyword)
                        .orElse(PopularSearchKeyword.create(keyword));

                existingKeyword.incrementCount(score.intValue()); // 기존 값 증가
                popularSearchKeywordRepository.save(existingKeyword);
            }
        }

        redisTemplate.opsForZSet().remove(POPULAR_SEARCH_KEY, keywords.toArray());
        log.info("인기 검색어 동기화 완료");
    }
}