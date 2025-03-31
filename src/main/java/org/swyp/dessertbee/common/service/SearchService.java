package org.swyp.dessertbee.common.service;

import org.swyp.dessertbee.common.dto.UserSearchHistoryDto;

import java.util.List;
import java.util.Map;

public interface SearchService {
    /** 공백 제거 */
    String removeTrailingSpaces(String input);

    /** 최근 검색어 저장 (인증된 사용자만) */
    void saveRecentSearch(Long userId, String keyword);

    /** 검색어 10개 초과 시 예전 검색어 삭제 */
    void deleteOldSearches(Long userId);

    /** 최근 검색어 조회 */
    List<UserSearchHistoryDto> getRecentSearches(Long userId);

    /** 최근 검색어 삭제 */
    boolean deleteRecentSearch(Long userId, Long searchId);

    /** 최근 검색어 전체 삭제 */
    void deleteAllRecentSearches(Long userId);

    /**
     * 검색어 저장 (Redis)
     */
    void savePopularSearch(String keyword);

    /**
     * 실시간 인기 검색어 조회 (이전 검색 횟수 차이 포함)
     */
    Map<String, Object> getPopularSearchesWithDifference(int limit);

    /**
     * Redis 인기 검색어 데이터 초기화 (테스트용)
     */
    void clearPopularSearchCache();

    void syncPopularSearchesToDB();                // 1분 동기화용
    void midnightSyncPopularSearchesToDB();        // 자정 동기화용
}
