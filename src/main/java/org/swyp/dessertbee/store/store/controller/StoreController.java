package org.swyp.dessertbee.store.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.store.dto.response.StoreDetailResponse;
import org.swyp.dessertbee.store.store.dto.response.StoreMapResponse;
import org.swyp.dessertbee.store.store.dto.response.StoreSummaryResponse;
import org.swyp.dessertbee.store.store.service.StoreService;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper 추가

    /** 가게 등록 */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreDetailResponse> createStore(
            @RequestPart("request") String requestJson,  // JSON 문자열로 받음
            @RequestPart(value = "storeImageFiles", required = false) List<MultipartFile> storeImageFiles,
            @RequestPart(value = "ownerPickImageFiles", required = false) List<MultipartFile> ownerPickImageFiles,
            @RequestPart(value = "menuImageFiles", required = false) List<MultipartFile> menuImageFiles) {

        try {
            // JSON 문자열을 StoreCreateRequest 객체로 변환
            StoreCreateRequest request = objectMapper.readValue(requestJson, StoreCreateRequest.class);

            storeImageFiles = storeImageFiles != null ? storeImageFiles : Collections.emptyList();
            ownerPickImageFiles = ownerPickImageFiles != null ? ownerPickImageFiles : Collections.emptyList();
            menuImageFiles = menuImageFiles != null ? menuImageFiles : Collections.emptyList();

            // 가게 생성
            StoreDetailResponse response = storeService.createStore(request, storeImageFiles, ownerPickImageFiles, menuImageFiles);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();  // 에러 로그 출력
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /** 반경 내 가게 조회 */
    @GetMapping("/map")
    public List<StoreMapResponse> getStoresByLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radius) {
        return storeService.getStoresByLocation(latitude, longitude, radius);
    }

    /** 가게 간략 정보 조회 */
    @GetMapping("/{storeUuid}/summary")
    public StoreSummaryResponse getStoreSummary(@PathVariable UUID storeUuid) {
        return storeService.getStoreSummary(storeUuid);
    }

    /** 가게 상세 정보 조회 */
    @GetMapping("/{storeUuid}/details")
    public StoreDetailResponse getStoreDetails(@PathVariable UUID storeUuid, UserEntity user) {
        if (storeUuid == null) {
            throw new IllegalArgumentException("storeUuid가 요청에서 누락되었습니다.");
        }
        return storeService.getStoreDetails(storeUuid, user);
    }

}
