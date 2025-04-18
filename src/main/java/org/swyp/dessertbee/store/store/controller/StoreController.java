package org.swyp.dessertbee.store.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.store.store.dto.response.StoreSearchResponse;
import org.swyp.dessertbee.search.exception.SearchExceptions;
import org.swyp.dessertbee.search.service.SearchService;
import org.swyp.dessertbee.store.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.store.dto.request.StoreUpdateRequest;
import org.swyp.dessertbee.store.store.dto.response.*;
import org.swyp.dessertbee.store.store.service.StoreService;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.service.UserService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Tag(name = "Store", description = "가게 관련 API")
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final SearchService searchService;
    private final UserService userService;

    /** 가게 등록 */
    @Operation(summary = "가게 등록 (completed)", description = "업주가 가게를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "가게 등록 성공")
    @ApiErrorResponses({
            ErrorCode.STORE_CREATION_FAILED,
            ErrorCode.STORE_SERVICE_ERROR,
            ErrorCode.STORE_TAG_SAVE_FAILED,
            ErrorCode.INVALID_TAG_SELECTION,
            ErrorCode.INVALID_TAG_INCLUDED,
            ErrorCode.STORE_HOLIDAY_TERM_ERROR,
            ErrorCode.STORE_HOLIDAY_TYPE_ERROR,
            ErrorCode.STORE_TAG_SAVE_FAILED
    })
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createStore(
            @RequestPart("request") StoreCreateRequest request,
            @RequestPart(value = "storeImageFiles", required = false) List<MultipartFile> storeImageFiles,
            @RequestPart(value = "ownerPickImageFiles", required = false) List<MultipartFile> ownerPickImageFiles,
            @RequestPart(value = "menuImageFiles", required = false) List<MultipartFile> menuImageFiles) {

        // 가게 생성
        storeService.createStore(request, storeImageFiles, ownerPickImageFiles, menuImageFiles);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** 업주가 등록한 가게 조회 */
    @Operation(summary = "업주 UUID로 등록된 가게 목록 조회 (completed)", description = "ownerUuid에 해당하는 가게들의 ID, UUID, 이름 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "업주가 등록한 가게 목록 조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = StoreShortInfoResponse.class))))
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @GetMapping("/owner")
    public ResponseEntity<List<StoreShortInfoResponse>> getStoresByOwner() {
        UUID ownerUuid = userService.getCurrentUser().getUserUuid();
        List<StoreShortInfoResponse> stores = storeService.getStoresByOwnerUuid(ownerUuid);
        return ResponseEntity.ok(stores);
    }

    /** 반경 내 가게 조회 */
    @Operation(
            summary = "반경 내 가게 조회 (completed)",
            description = """
        위치(latitude, longitude)와 반경(radius)을 기준으로 가게 목록을 조회합니다.
        아래 3가지 방식 중 **하나만** 사용할 수 있습니다:

        1. 기본 요청 (전체 가게 조회)
        2. preferenceTagIds로 필터링 (복수 개 가능, 예: preferenceTagIds=1&preferenceTagIds=2)
        3. 검색어(searchKeyword) 기반 조회 - WEB 전용 (예: 검색어를 URL 인코딩해서 전달)
        """
    )
    @ApiResponse(
            responseCode = "200",
            description = "지도 반경 내 가게 조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = StoreMapResponse.class)))
    )
    @ApiErrorResponses({ErrorCode.STORE_MAP_READ_FAILED, ErrorCode.STORE_SERVICE_ERROR,
            ErrorCode.STORE_SEARCH_FAILED, ErrorCode.PREFERENCES_NOT_FOUND})
    @GetMapping("/map")
    public List<StoreMapResponse> getStoresByLocation(
            @Parameter(description = "위도", example = "37.785834", required = true)
            @RequestParam Double latitude,

            @Parameter(description = "경도", example = "122.406417", required = true)
            @RequestParam Double longitude,

            @Parameter(description = "검색 반경(m)", example = "3.0", required = true)
            @RequestParam Double radius,

            @Parameter(
                    description = """
                (선택) 사용자 선호 태그 ID 리스트
                복수 개 전달 가능 → 예: preferenceTagIds=1&preferenceTagIds=2
                searchKeyword와 동시 사용 불가
                """,
                    example = "1"
            )
            @RequestParam(required = false) List<Long> preferenceTagIds,

            @Parameter(
                    description = """
                (선택) 검색어 (예: 매장명, 태그 등)
                반드시 URL 인코딩 필요 → 예: '커플' → %EC%BB%A4%ED%94%8C
                preferenceTagIds와 동시 사용 불가
                """,
                    example = "%EC%BB%A4%ED%94%8C"
            )
            @RequestParam(required = false) String searchKeyword) {

        if (searchKeyword != null) {
            searchKeyword = URLDecoder.decode(searchKeyword, StandardCharsets.UTF_8);
            searchKeyword = searchService.removeTrailingSpaces(searchKeyword);

            UserEntity user = userService.getCurrentUser();
            // 인증된 사용자일 경우 >> 최근 검색어 저장
            if (user != null) {
                searchService.saveRecentSearch(user.getId(), searchKeyword);
            }
            // 인증 여부와 관계없이 인기 검색어 저장
            searchService.savePopularSearch(searchKeyword);
        }

        if (preferenceTagIds != null && !preferenceTagIds.isEmpty()) {
            return storeService.getStoresByLocationAndTags(latitude, longitude, radius, preferenceTagIds);
        } else if (searchKeyword != null) {
            return storeService.getStoresByLocationAndKeyword(latitude, longitude, radius, searchKeyword);
        } else {
            return storeService.getStoresByLocation(latitude, longitude, radius);
        }
    }

    /** 검색어 기반 가게 검색 */
    @Operation(
            summary = "검색어 기반 가게 검색 - APP 전용 (completed)",
            description = """
        검색어(searchKeyword)를 기준으로 가게 목록을 검색합니다.
        검색어는 URL 인코딩되어야 하며, 예: '케이크' → %EC%BC%80%EC%9D%B4%ED%81%AC
        검색된 결과는 앱 내 추천 기능에 사용될 수 있습니다.
        헤더에 'Platform-Type: app'이 반드시 포함되어야 합니다.
        """
    )
    @ApiResponse(
            responseCode = "200",
            description = "검색어 기반 가게 검색 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = StoreSearchResponse.class)))
    )
    @ApiErrorResponses({
            ErrorCode.INVALID_PLATFORM_VALUE,
            ErrorCode.SEARCH_KEYWORD_NOT_FOUND,
            ErrorCode.SEARCH_SERVICE_ERROR,
            ErrorCode.RECENT_KEYWORD_CREATION_FAILED,
            ErrorCode.POPULAR_KEYWORD_CREATION_FAILED,
            ErrorCode.ELASTICSEARCH_COMMUNICATION_FAILED
    })
    @GetMapping("/search")
    public List<StoreSearchResponse> searchStores(
            @Parameter(description = "플랫폼 구분자. 'app'만 허용됨", example = "app", required = true)
            @RequestHeader("Platform-Type") String platformType,

            @Parameter(description = "검색어 (URL 인코딩 필수)", example = "%EC%BC%80%EC%9D%B4%ED%81%AC", required = true)
            @RequestParam String searchKeyword) {

        if (!"app".equalsIgnoreCase(platformType)) {
            throw new SearchExceptions.InvalidPlatformException("해당 API를 실행하려면 'app'을 플랫폼 헤더로 지정해야합니다.");
        }

        searchKeyword = URLDecoder.decode(searchKeyword, StandardCharsets.UTF_8);
        searchKeyword = searchService.removeTrailingSpaces(searchKeyword);

        UserEntity user = userService.getCurrentUser();
        if (user != null) {
            searchService.saveRecentSearch(user.getId(), searchKeyword);
        }
        searchService.savePopularSearch(searchKeyword);

        return storeService.searchStores(searchKeyword);
    }

    /**
     * 반경 내 가게 조회 (인증된 사용자의 취향 태그 기반)
     */
    @Operation(summary = "반경 내 사용자 취향 가게 조회 (completed)", description = "반경 내에서 사용자의 취향 태그를 가진 가게를 조회합니다.")
    @ApiResponse( responseCode = "200", description = "지도 반경 내 사용자 취향 태그 가진 가게 조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = StoreMapResponse.class))))
    @ApiErrorResponses({ErrorCode.PREFERENCE_STORE_READ_FAILED, ErrorCode.STORE_SERVICE_ERROR,
            ErrorCode.INVALID_USER_STATUS, ErrorCode.USER_PREFERENCES_NOT_FOUND})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @GetMapping("/map/my-preferences")
    public ResponseEntity<List<StoreMapResponse>> getStoresByMyPreferences(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("radius") Double radius) {

        List<StoreMapResponse> storeMapResponses = storeService.getStoresByMyPreferences(latitude, longitude, radius);
        return ResponseEntity.ok(storeMapResponses);
    }

    /** 가게 간략 정보 조회 */
    @Operation(summary = "가게 간략 정보 조회 (completed)", description = "가게의 간략한 정보를 조회합니다.")
    @ApiResponse( responseCode = "200", description = "가게 간략 정보 조회 성공", content = @Content(schema = @Schema(implementation = StoreSummaryResponse.class)))
    @ApiErrorResponses({ErrorCode.STORE_NOT_FOUND, ErrorCode.STORE_SERVICE_ERROR,
            ErrorCode.STORE_INFO_READ_FAILED})
    @GetMapping("/{storeUuid}/summary")
    public StoreSummaryResponse getStoreSummary(@PathVariable UUID storeUuid) {
        return storeService.getStoreSummary(storeUuid);
    }

    /** 가게 상세 정보 조회 */
    @Operation(summary = "가게 상세 정보 조회 (completed)", description = "가게의 상세한 정보를 조회합니다.")
    @ApiResponse( responseCode = "200", description = "가게 상세 정보 조회 성공", content = @Content(schema = @Schema(implementation = StoreDetailResponse.class)))
    @ApiErrorResponses({ErrorCode.STORE_NOT_FOUND, ErrorCode.STORE_SERVICE_ERROR,
            ErrorCode.STORE_INFO_READ_FAILED, ErrorCode.STORE_NOTICE_SERVICE_ERROR,
            ErrorCode.MENU_SERVICE_ERROR})
    @GetMapping("/{storeUuid}/details")
    public StoreDetailResponse getStoreDetails(@PathVariable UUID storeUuid) {
        return storeService.getStoreDetails(storeUuid);
    }

    /** 가게 수정 */
    @Operation(summary = "가게 수정", description = "업주가 가게의 정보를 수정합니다.")
    @ApiResponse( responseCode = "200", description = "가게 수정 성공")
    @ApiErrorResponses({ErrorCode.STORE_NOT_FOUND, ErrorCode.STORE_SERVICE_ERROR,
            ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.STORE_UPDATE_FAILED,
            ErrorCode.STORE_HOLIDAY_TERM_ERROR, ErrorCode.STORE_HOLIDAY_TYPE_ERROR,
            ErrorCode.STORE_TAG_SAVE_FAILED})
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @PatchMapping(value = "/{storeUuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreInfoResponse> updateStore(
            @PathVariable UUID storeUuid,
            @RequestPart("request") StoreUpdateRequest request,
            @RequestPart(value = "storeImageFiles", required = false) List<MultipartFile> storeImageFiles,
            @RequestPart(value = "ownerPickImageFiles", required = false) List<MultipartFile> ownerPickImageFiles) {

        StoreInfoResponse updatedInfo = storeService.updateStore(storeUuid, request, storeImageFiles, ownerPickImageFiles);
        return ResponseEntity.ok(updatedInfo);
    }

    /** 가게 삭제 */
    @Operation(summary = "가게 삭제 (completed)", description = "업주가 가게를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "가게 삭제 성공")
    @ApiErrorResponses({ErrorCode.STORE_NOT_FOUND, ErrorCode.STORE_SERVICE_ERROR,
            ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.STORE_DELETE_FAILED})
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @DeleteMapping("/{storeUuid}")
    public ResponseEntity<Void> deleteStore(@PathVariable UUID storeUuid) {
        try {
            storeService.deleteStore(storeUuid);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
