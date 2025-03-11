package org.swyp.dessertbee.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.dto.PopularSearchResponse;
import org.swyp.dessertbee.common.dto.UserSearchHistoryDto;
import org.swyp.dessertbee.common.service.SearchService;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final UserService userService;

    /** 최근 검색어 조회 API */
    @GetMapping("/recent")
    public ResponseEntity<List<UserSearchHistoryDto>> getRecentSearches() {
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<UserSearchHistoryDto> recentSearches = searchService.getRecentSearches(user.getId());
        return ResponseEntity.ok(recentSearches);
    }

    /** 특정 검색어 삭제 */
    @DeleteMapping("/recent/{searchId}")
    public ResponseEntity<Void> deleteRecentSearch(@PathVariable Long searchId) {
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        searchService.deleteRecentSearch(user.getId(), searchId);
        return ResponseEntity.noContent().build();
    }

    /** 모든 검색어 삭제 */
    @DeleteMapping("/recent/all")
    public ResponseEntity<Void> deleteAllRecentSearches() {
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        searchService.deleteAllRecentSearches(user.getId());
        return ResponseEntity.noContent().build();
    }

    /** 실시간 인기 검색어 조회 API (이전 검색 횟수 차이 + 업데이트 시간 포함) */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularSearches(
            @RequestParam(defaultValue = "10") int limit
    ) {
        Map<String, Object> response = searchService.getPopularSearchesWithDifference(limit);
        return ResponseEntity.ok(response);
    }

    /** Redis 인기 검색어 초기화 API (테스트용) */
    @GetMapping("/popular/clear")
    public ResponseEntity<String> clearPopularSearchCache() {
        searchService.clearPopularSearchCache();
        return ResponseEntity.ok("✅ Redis 캐싱된 인기 검색어 데이터가 초기화되었습니다.");
    }

}