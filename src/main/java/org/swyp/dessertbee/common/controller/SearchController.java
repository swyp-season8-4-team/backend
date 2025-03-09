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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/search")
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

    /** 실시간 인기 검색어 조회 API */
    @GetMapping("/popular")
    public ResponseEntity<List<PopularSearchResponse>> getPopularSearches(
            @RequestParam(defaultValue = "10") int limit
    ) {
        Set<String> keywords = searchService.getPopularSearches(limit);

        List<PopularSearchResponse> response = new ArrayList<>();
        int rank = 1;
        for (String keyword : keywords) {
            response.add(new PopularSearchResponse(keyword, rank++));
        }

        return ResponseEntity.ok(response);
    }

}