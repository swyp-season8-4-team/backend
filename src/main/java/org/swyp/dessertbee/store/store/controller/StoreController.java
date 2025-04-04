package org.swyp.dessertbee.store.store.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import org.swyp.dessertbee.common.service.SearchService;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.store.dto.request.StoreUpdateRequest;
import org.swyp.dessertbee.store.store.dto.response.StoreDetailResponse;
import org.swyp.dessertbee.store.store.dto.response.StoreMapResponse;
import org.swyp.dessertbee.store.store.dto.response.StoreSummaryResponse;
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
            ErrorCode.INVALID_TAG_INCLUDED
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

    /** 반경 내 가게 조회 */
    @Operation(summary = "반경 내 가게 조회 (completed)", description = "지도 반경 내 가게를 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "지도 반경 내 가게 조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = StoreMapResponse.class)))
    )
    @ApiErrorResponses({ErrorCode.STORE_MAP_READ_FAILED, ErrorCode.STORE_SERVICE_ERROR, ErrorCode.STORE_SEARCH_FAILED})
    @GetMapping("/map")
    public List<StoreMapResponse> getStoresByLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radius,
            @RequestParam(required = false) List<Long> preferenceTagIds,
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

    /**
     * 반경 내 가게 조회 (인증된 사용자의 취향 태그 기반)
     */
    @Operation(summary = "반경 내 사용자 취향 가게 조회 (completed)", description = "반경 내에서 사용자의 취향 태그를 가진 가게를 조회합니다.")
    @ApiResponse( responseCode = "200", description = "지도 반경 내 사용자 취향 태그 가진 가게 조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = StoreMapResponse.class))))
    @ApiErrorResponses({ErrorCode.PREFERENCE_STORE_READ_FAILED, ErrorCode.STORE_SERVICE_ERROR})
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
    @Operation(summary = "가게 간략 정보 조회", description = "가게의 간략한 정보를 조회합니다.")
    @ApiResponse( responseCode = "200", description = "가게 간략 정보 조회 성공", content = @Content(schema = @Schema(implementation = StoreSummaryResponse.class)))
    @ApiErrorResponses({ErrorCode.STORE_NOT_FOUND, ErrorCode.STORE_SERVICE_ERROR, ErrorCode.STORE_INFO_READ_FAILED})
    @GetMapping("/{storeUuid}/summary")
    public StoreSummaryResponse getStoreSummary(@PathVariable UUID storeUuid) {
        return storeService.getStoreSummary(storeUuid);
    }

    /** 가게 상세 정보 조회 */
    @Operation(summary = "가게 상세 정보 조회", description = "가게의 상세한 정보를 조회합니다.")
    @ApiResponse( responseCode = "200", description = "가게 상세 정보 조회 성공", content = @Content(schema = @Schema(implementation = StoreDetailResponse.class)))
    @ApiErrorResponses({ErrorCode.STORE_NOT_FOUND, ErrorCode.STORE_SERVICE_ERROR, ErrorCode.STORE_INFO_READ_FAILED})
    @GetMapping("/{storeUuid}/details")
    public StoreDetailResponse getStoreDetails(@PathVariable UUID storeUuid) {
        return storeService.getStoreDetails(storeUuid);
    }

    /** 가게 수정 */
    @Operation(summary = "가게 수정", description = "업주가 가게의 정보를 수정합니다.")
    @ApiResponse( responseCode = "200", description = "가게 수정 성공")
    @ApiErrorResponses({ErrorCode.STORE_NOT_FOUND, ErrorCode.STORE_SERVICE_ERROR, ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.STORE_UPDATE_FAILED})
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @PatchMapping(value = "/{storeUuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateStore(
            @PathVariable UUID storeUuid,
            @RequestPart("request") StoreUpdateRequest request,
            @RequestPart(value = "storeImageFiles", required = false) List<MultipartFile> storeImageFiles,
            @RequestPart(value = "ownerPickImageFiles", required = false) List<MultipartFile> ownerPickImageFiles,
            @RequestPart(value = "menuImageFiles", required = false) List<MultipartFile> menuImageFiles) {

        storeService.updateStore(storeUuid, request, storeImageFiles, ownerPickImageFiles, menuImageFiles);
        return ResponseEntity.ok().build();
    }

    /** 가게 삭제 */
    @Operation(summary = "가게 삭제 (completed)", description = "업주가 가게를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "가게 삭제 성공")
    @ApiErrorResponses({ErrorCode.STORE_NOT_FOUND, ErrorCode.STORE_SERVICE_ERROR, ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.STORE_DELETE_FAILED})
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
