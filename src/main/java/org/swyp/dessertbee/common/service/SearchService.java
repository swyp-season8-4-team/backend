package org.swyp.dessertbee.common.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.entity.UserSearchHistory;
import org.swyp.dessertbee.common.repository.UserSearchHistoryRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserSearchHistoryRepository searchHistoryRepository;

    private static final int MAX_RECENT_SEARCHES = 10;

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

        // 최대 10개 유지 (초과 시 오래된 검색어 삭제)
        deleteOldSearches(userId);
    }

    /** 🔹 초과 검색어 삭제 (최신 10개 유지) */
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
}