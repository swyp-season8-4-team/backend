package org.swyp.dessertbee.store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.dto.response.StoreDetailResponse;
import org.swyp.dessertbee.store.dto.response.StoreMapResponse;
import org.swyp.dessertbee.store.dto.response.StoreSummaryResponse;
import org.swyp.dessertbee.store.service.StoreService;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /** 가게 등록 */
    @PostMapping
    public ResponseEntity<StoreDetailResponse> createStore(@RequestBody @Valid StoreCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.createStore(request));
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
    @GetMapping("/{id}/summary")
    public StoreSummaryResponse getStoreSummary(@PathVariable Long id) {
        return storeService.getStoreSummary(id);
    }

    /** 가게 상세 정보 조회 */
    @GetMapping("/{id}/details")
    public StoreDetailResponse getStoreDetails(@PathVariable Long id) {
        return storeService.getStoreDetails(id);
    }

}
