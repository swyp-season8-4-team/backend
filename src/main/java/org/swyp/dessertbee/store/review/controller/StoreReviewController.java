package org.swyp.dessertbee.store.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.review.dto.request.StoreReviewCreateRequest;
import org.swyp.dessertbee.store.review.dto.request.StoreReviewUpdateRequest;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.review.service.StoreReviewService;

import java.util.List;
import java.util.UUID;

@Tag(name = "StoreReview", description = "가게 한줄리뷰 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeUuid}/reviews")
public class StoreReviewController {

    private final StoreReviewService storeReviewService;

    /** 리뷰 등록 */
    @Operation(summary = "한줄 리뷰 등록", description = "한줄 리뷰를 등록합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<StoreReviewResponse> createReview(
            @PathVariable UUID storeUuid,
            @RequestPart(value = "request") StoreReviewCreateRequest request,
            @RequestPart(required = false) List<MultipartFile> images) {

        log.info("📥 요청 데이터: {}", request);
        StoreReviewResponse response = storeReviewService.createReview(storeUuid, request, images);
        return ResponseEntity.ok(response);
    }

    /** 특정 가게 리뷰 조회 */
    @Operation(summary = "한줄 리뷰 조회", description = "한줄 리뷰를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<StoreReviewResponse>> getReviews(@PathVariable UUID storeUuid) {
        List<StoreReviewResponse> reviews = storeReviewService.getReviewsByStoreId(storeUuid);
        return ResponseEntity.ok(reviews);
    }

    /** 리뷰 수정 */
    @Operation(summary = "한줄 리뷰 수정", description = "한줄 리뷰를 수장합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @PatchMapping(value = "/{reviewUuid}", consumes = "multipart/form-data")
    public ResponseEntity<StoreReviewResponse> updateReview(
            @PathVariable UUID storeUuid,
            @PathVariable UUID reviewUuid,
            @RequestPart(value = "request") StoreReviewUpdateRequest request,
            @RequestPart(required = false) List<MultipartFile> newImages) {

        log.info("📥 요청 데이터: {}", request);
        StoreReviewResponse response = storeReviewService.updateReview(storeUuid, reviewUuid, request, newImages);
        return ResponseEntity.ok(response);
    }

    /** 리뷰 삭제 */
    @Operation(summary = "한줄 리뷰 삭제", description = "한줄 리뷰를 삭제합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @DeleteMapping("/{reviewUuid}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID storeUuid, @PathVariable UUID reviewUuid) {
        storeReviewService.deleteReview(storeUuid, reviewUuid);
        return ResponseEntity.noContent().build();
    }
}
