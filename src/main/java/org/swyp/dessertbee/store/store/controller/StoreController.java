package org.swyp.dessertbee.store.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.preference.entity.UserPreferenceEntity;
import org.swyp.dessertbee.store.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.store.dto.request.StoreUpdateRequest;
import org.swyp.dessertbee.store.store.dto.response.StoreDetailResponse;
import org.swyp.dessertbee.store.store.dto.response.StoreMapResponse;
import org.swyp.dessertbee.store.store.dto.response.StoreSummaryResponse;
import org.swyp.dessertbee.store.store.service.StoreService;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.*;

@Tag(name = "Store", description = "가게 관련 API")
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final ObjectMapper objectMapper;

    /** 가게 등록 */
    @Operation(summary = "가게 등록", description = "업주가 가게를 등록합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_OWNER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreDetailResponse> createStore(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "storeImageFiles", required = false) List<MultipartFile> storeImageFiles,
            @RequestPart(value = "ownerPickImageFiles", required = false) List<MultipartFile> ownerPickImageFiles,
            @RequestPart(value = "menuImageFiles", required = false) List<MultipartFile> menuImageFiles) {

        try {
            StoreCreateRequest request = objectMapper.readValue(requestJson, StoreCreateRequest.class);

            storeImageFiles = storeImageFiles != null ? storeImageFiles : Collections.emptyList();
            ownerPickImageFiles = ownerPickImageFiles != null ? ownerPickImageFiles : Collections.emptyList();
            menuImageFiles = menuImageFiles != null ? menuImageFiles : Collections.emptyList();

            // 가게 생성
            StoreDetailResponse response = storeService.createStore(request, storeImageFiles, ownerPickImageFiles, menuImageFiles);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /** 반경 내 가게 조회 */
    @Operation(summary = "반경 내 가게 조회", description = "지도 반경 내 가게를 조회합니다.")
    @GetMapping("/map")
    public List<StoreMapResponse> getStoresByLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radius,
            @RequestParam(required = false) List<Long> preferenceTagIds,
            @RequestParam(required = false) String searchKeyword) {

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
    @Operation(summary = "반경 내 사용자 취향 가게 조회", description = "반경 내에서 사용자의 취향 태그를 가진 가게를 조회합니다.")
    @GetMapping("/map/my-preferences")
    public ResponseEntity<List<StoreMapResponse>> getStoresByMyPreferences(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("radius") Double radius,
            @AuthenticationPrincipal UserEntity user) {

        List<StoreMapResponse> storeMapResponses = storeService.getStoresByMyPreferences(latitude, longitude, radius, user);
        return ResponseEntity.ok(storeMapResponses);
    }

    /** 가게 간략 정보 조회 */
    @Operation(summary = "가게 간략 정보 조회", description = "가게의 간략한 정보를 조회합니다.")
    @GetMapping("/{storeUuid}/summary")
    public StoreSummaryResponse getStoreSummary(@PathVariable UUID storeUuid) {
        return storeService.getStoreSummary(storeUuid);
    }

    /** 가게 상세 정보 조회 */
    @Operation(summary = "가게 상세 정보 조회", description = "가게의 상세한 정보를 조회합니다.")
    @GetMapping("/{storeUuid}/details")
    public StoreDetailResponse getStoreDetails(@PathVariable UUID storeUuid, UserEntity user) {
        return storeService.getStoreDetails(storeUuid, user);
    }

    /** 가게 수정 */
    @Operation(summary = "가게 수정", description = "업주가 가게의 정보를 수정합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_OWNER')")
    @PatchMapping(value = "/{storeUuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreDetailResponse> updateStore(
            @PathVariable UUID storeUuid,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "storeImageFiles", required = false) List<MultipartFile> storeImageFiles,
            @RequestPart(value = "ownerPickImageFiles", required = false) List<MultipartFile> ownerPickImageFiles,
            @RequestPart(value = "menuImageFiles", required = false) List<MultipartFile> menuImageFiles) {
        try {
            // StoreUpdateRequest는 StoreCreateRequest와 유사한 구조를 가정합니다.
            StoreUpdateRequest request = objectMapper.readValue(requestJson, StoreUpdateRequest.class);

            // null 체크 처리
            storeImageFiles = (storeImageFiles != null) ? storeImageFiles : Collections.emptyList();
            ownerPickImageFiles = (ownerPickImageFiles != null) ? ownerPickImageFiles : Collections.emptyList();
            menuImageFiles = (menuImageFiles != null) ? menuImageFiles : Collections.emptyList();

            StoreDetailResponse updatedStore = storeService.updateStore(storeUuid, request,
                    storeImageFiles, ownerPickImageFiles, menuImageFiles);
            return ResponseEntity.ok(updatedStore);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /** 가게 삭제 */
    @Operation(summary = "가게 삭제", description = "업주가 가게를 삭제합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_OWNER')")
    @DeleteMapping("/{storeUuid}")
    public ResponseEntity<Void> deleteStore(@PathVariable UUID storeUuid,
                                            @AuthenticationPrincipal UserEntity user) {
        try {
            storeService.deleteStore(storeUuid, user);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
