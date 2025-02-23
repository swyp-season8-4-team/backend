package org.swyp.dessertbee.store.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final ObjectMapper objectMapper;

    /** 가게 등록 */
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
    @GetMapping("/map")
    public List<StoreMapResponse> getStoresByLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radius,
            @RequestParam(required = false) Long preferenceTagId,
            @RequestParam(required = false) String searchKeyword) {

        if (preferenceTagId != null) {
            return storeService.getStoresByLocationAndTag(latitude, longitude, radius, preferenceTagId);
        } else if (searchKeyword != null) {
            return storeService.getStoresByLocationAndKeyword(latitude, longitude, radius, searchKeyword);
        } else {
            return storeService.getStoresByLocation(latitude, longitude, radius);
        }
    }

    /**
     * 반경 내 가게 조회 (인증된 사용자의 취향 태그 기반)
     */
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
    @GetMapping("/{storeUuid}/summary")
    public StoreSummaryResponse getStoreSummary(@PathVariable UUID storeUuid) {
        return storeService.getStoreSummary(storeUuid);
    }

    /** 가게 상세 정보 조회 */
    @GetMapping("/{storeUuid}/details")
    public StoreDetailResponse getStoreDetails(@PathVariable UUID storeUuid, UserEntity user) {
        return storeService.getStoreDetails(storeUuid, user);
    }

    /** 가게 수정 */
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_OWNER')")
    @PatchMapping(value = "/{storeUuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreDetailResponse> updateStore(
            @PathVariable UUID storeUuid,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "storeImageFiles", required = false) List<MultipartFile> storeImageFiles,
            @RequestPart(value = "ownerPickImageFiles", required = false) List<MultipartFile> ownerPickImageFiles,
            @RequestPart(value = "menuImageFiles", required = false) List<MultipartFile> menuImageFiles,
            @AuthenticationPrincipal UserEntity user) {
        try {
            // StoreUpdateRequest는 StoreCreateRequest와 유사한 구조를 가정합니다.
            StoreUpdateRequest request = objectMapper.readValue(requestJson, StoreUpdateRequest.class);

            // null 체크 처리
            storeImageFiles = (storeImageFiles != null) ? storeImageFiles : Collections.emptyList();
            ownerPickImageFiles = (ownerPickImageFiles != null) ? ownerPickImageFiles : Collections.emptyList();
            menuImageFiles = (menuImageFiles != null) ? menuImageFiles : Collections.emptyList();

            StoreDetailResponse updatedStore = storeService.updateStore(storeUuid, request,
                    storeImageFiles, ownerPickImageFiles, menuImageFiles, user);
            return ResponseEntity.ok(updatedStore);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /** 가게 삭제 */
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
