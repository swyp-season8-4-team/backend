package org.swyp.dessertbee.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.dto.PopularSearchesList;
import org.swyp.dessertbee.common.dto.UserSearchHistoryDto;
import org.swyp.dessertbee.common.exception.SearchExceptions.*;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.service.SearchService;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.service.UserService;

import java.util.List;

@Tag(name = "Search Keyword", description = "검색어 관련 API")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final UserService userService;

    /** 최근 검색어 조회 API */
    @Operation(summary = "최근 검색어 조회 (completed)", description = "최근 검색어를 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "최근 검색어 조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserSearchHistoryDto.class)))
    )
    @ApiErrorResponses({ErrorCode.SEARCH_SERVICE_ERROR})
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/recent")
    public ResponseEntity<List<UserSearchHistoryDto>> getRecentSearches() {
        UserEntity user = userService.getCurrentUser();

        List<UserSearchHistoryDto> recentSearches = searchService.getRecentSearches(user.getId());
        return ResponseEntity.ok(recentSearches);
    }

    /** 특정 검색어 삭제 */
    @Operation(summary = "최근 검색어 삭제 (completed)", description = "최근 검색어를 삭제합니다.")
    @ApiResponse( responseCode = "204", description = "최근 검색어 삭제 성공")
    @ApiErrorResponses({ErrorCode.SEARCH_SERVICE_ERROR, ErrorCode.RECENT_KEYWORD_DELETE_FAILED})
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/recent/{searchId}")
    public ResponseEntity<Void> deleteRecentSearch(@PathVariable Long searchId) {
        UserEntity user = userService.getCurrentUser();

        boolean deleted = searchService.deleteRecentSearch(user.getId(), searchId);
        if (!deleted) {
            throw new KeywordNotFoundException();
        }

        return ResponseEntity.noContent().build();
    }

    /** 모든 검색어 삭제 */
    @Operation(summary = "최근 검색어 전체 삭제 (completed)", description = "최근 검색어를 전체 삭제합니다.")
    @ApiResponse( responseCode = "204", description = "최근 검색어 전체 삭제 성공")
    @ApiErrorResponses({ErrorCode.SEARCH_SERVICE_ERROR, ErrorCode.RECENT_KEYWORD_DELETE_FAILED})
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/recent/all")
    public ResponseEntity<Void> deleteAllRecentSearches() {
        UserEntity user = userService.getCurrentUser();

        searchService.deleteAllRecentSearches(user.getId());

        return ResponseEntity.noContent().build();
    }

    /** 실시간 인기 검색어 조회 API (이전 검색 횟수 차이 + 업데이트 시간 포함) */
    @Operation(summary = "실시간 인기 검색어 조회 (completed)", description = "실시간으로 인기 검색어를 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "실시간 인기 검색어 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PopularSearchesList.class)
            )
    )
    @ApiErrorResponses({ErrorCode.SEARCH_SERVICE_ERROR})
    @GetMapping("/popular")
    public ResponseEntity<PopularSearchesList> getPopularSearches(
            @RequestParam(defaultValue = "10") int limit
    ) {
        PopularSearchesList response = searchService.getPopularSearchesWithDifference(limit);
        return ResponseEntity.ok(response);
    }

    /** Redis 인기 검색어 초기화 API (테스트용) */
    @Operation(summary = "(테스트용) 인기 검색어 초기화", description = "인기 검색어를 초기화합니다. 이것은 개발자 테스트용으로 실제 서비스에 사용되지 않습니다.")
    @ApiResponse( responseCode = "200", description = "인기 검색어 데이터 초기화 성공")
    @ApiErrorResponses({ErrorCode.SEARCH_SERVICE_ERROR, ErrorCode.POPULAR_KEYWORD_INIT_FAILED})
    @GetMapping("/popular/clear")
    public ResponseEntity<String> clearPopularSearchCache() {
        searchService.clearPopularSearchCache();
        return ResponseEntity.ok("✅ Redis 캐싱된 인기 검색어 데이터가 초기화되었습니다.");
    }

}