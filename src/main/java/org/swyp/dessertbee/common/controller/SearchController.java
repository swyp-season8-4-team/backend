package org.swyp.dessertbee.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.common.dto.PopularSearchResponse;
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
    public ResponseEntity<List<String>> getRecentSearches() {
        UserEntity user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> recentSearches = searchService.getRecentSearches(user.getId());
        return ResponseEntity.ok(recentSearches);
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