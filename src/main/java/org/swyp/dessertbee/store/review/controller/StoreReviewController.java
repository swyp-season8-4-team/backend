package org.swyp.dessertbee.store.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.review.dto.request.StoreReviewCreateRequest;
import org.swyp.dessertbee.store.review.dto.request.StoreReviewUpdateRequest;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.review.service.StoreReviewService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeUuid}/reviews")
public class StoreReviewController {

    private final StoreReviewService storeReviewService;

    /** 리뷰 등록 */
    @PostMapping
    public ResponseEntity<StoreReviewResponse> createReview(
            @PathVariable UUID storeUuid,
            @RequestPart(value = "request") String requestJson,
            @RequestPart(required = false) List<MultipartFile> images) {

        // JSON String을 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        StoreReviewCreateRequest request;
        try {
            request = objectMapper.readValue(requestJson, StoreReviewCreateRequest.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 요청 양식 오류", e);
        }

        log.info("📥 요청 데이터: {}", request);
        StoreReviewResponse response = storeReviewService.createReview(storeUuid, request, images);
        return ResponseEntity.ok(response);
    }

    /** 특정 가게 리뷰 조회 */
    @GetMapping
    public ResponseEntity<List<StoreReviewResponse>> getReviews(@PathVariable UUID storeUuid) {
        List<StoreReviewResponse> reviews = storeReviewService.getReviewsByStoreId(storeUuid);
        return ResponseEntity.ok(reviews);
    }

    /** 리뷰 수정 */
    @PutMapping("/{reviewUuid}")
    public ResponseEntity<StoreReviewResponse> updateReview(
            @PathVariable UUID storeUuid,
            @PathVariable UUID reviewUuid,
            @RequestPart(value = "request") String requestJson,
            @RequestPart(required = false) List<MultipartFile> newImages) {

        ObjectMapper objectMapper = new ObjectMapper();
        StoreReviewUpdateRequest request;
        try {
            request = objectMapper.readValue(requestJson, StoreReviewUpdateRequest.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 요청 양식 오류", e);
        }

        StoreReviewResponse response = storeReviewService.updateReview(storeUuid, reviewUuid, request, newImages);
        return ResponseEntity.ok(response);
    }

    /** 리뷰 삭제 */
    @DeleteMapping("/{reviewUuid}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID storeUuid, @PathVariable UUID reviewUuid) {
        storeReviewService.deleteReview(storeUuid, reviewUuid);
        return ResponseEntity.noContent().build();
    }
}
