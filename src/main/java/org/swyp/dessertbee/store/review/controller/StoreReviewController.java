package org.swyp.dessertbee.store.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.store.menu.dto.response.MenuResponse;
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

    /** 오늘 작성한 리뷰 여부 조회 */
    @Operation(
            summary = "오늘 작성한 리뷰 여부 조회",
            description = "특정 유저가 특정 가게에 대해 오늘 작성한 리뷰가 있는지 여부를 반환합니다. (true/false)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "오늘 작성한 리뷰 여부 조회 성공",
            content = @Content(schema = @Schema(implementation = Boolean.class))
    )
    @ApiErrorResponses({ErrorCode.INVALID_STORE_UUID, ErrorCode.STORE_REVIEW_SERVICE_ERROR})
    @GetMapping("/today-exists")
    public ResponseEntity<Boolean> checkTodayReview(
            @PathVariable UUID storeUuid,
            @RequestParam UUID userUuid) {
        boolean exists = storeReviewService.hasTodayReview(storeUuid, userUuid);
        return ResponseEntity.ok(exists);
    }

    /** 리뷰 등록 */
    @Operation(summary = "한줄 리뷰 등록 (completed)", description = "한줄 리뷰를 등록합니다.")
    @ApiResponse( responseCode = "200", description = "한줄리뷰 등록 성공", content = @Content(schema = @Schema(implementation = StoreReviewResponse.class)))
    @ApiErrorResponses({ErrorCode.INVALID_STORE_UUID, ErrorCode.STORE_REVIEW_SERVICE_ERROR,
            ErrorCode.INVALID_STORE_REVIEW_UUID, ErrorCode.USER_NOT_FOUND,
            ErrorCode.STORE_REVIEW_CREATION_FAILED, ErrorCode.STORE_REVIEW_ALREADY_EXISTS_TODAY})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreReviewResponse> createReview(
            @PathVariable UUID storeUuid,
            @RequestPart(value = "request") StoreReviewCreateRequest request,
            @RequestPart(required = false) List<MultipartFile> images) {

        log.info("📥 요청 데이터: {}", request);
        StoreReviewResponse response = storeReviewService.createReview(storeUuid, request, images);
        return ResponseEntity.ok(response);
    }

    /** 특정 가게 리뷰 조회 */
    @Operation(summary = "한줄 리뷰 조회 (completed)", description = "한줄 리뷰를 조회합니다.")
    @ApiResponse( responseCode = "200", description = "한줄리뷰 조회성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = StoreReviewResponse.class))))
    @ApiErrorResponses({ErrorCode.INVALID_STORE_UUID, ErrorCode.STORE_REVIEW_SERVICE_ERROR})
    @GetMapping
    public ResponseEntity<List<StoreReviewResponse>> getReviews(@PathVariable UUID storeUuid) {
        List<StoreReviewResponse> reviews = storeReviewService.getReviewsByStoreUuid(storeUuid);
        return ResponseEntity.ok(reviews);
    }

    /** 리뷰 수정 */
    @Operation(summary = "한줄 리뷰 수정 (completed)", description = "한줄 리뷰를 수장합니다.")
    @ApiResponse( responseCode = "200", description = "한줄리뷰 수정 성공", content = @Content(schema = @Schema(implementation = StoreReviewResponse.class)))
    @ApiErrorResponses({ErrorCode.INVALID_STORE_UUID, ErrorCode.STORE_REVIEW_SERVICE_ERROR, ErrorCode.STORE_REVIEW_NOT_FOUND, ErrorCode.INVALID_STORE_REVIEW, ErrorCode.USER_NOT_FOUND, ErrorCode.STORE_REVIEW_UPDATE_FAILED})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @PatchMapping(value = "/{reviewUuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
    @Operation(summary = "한줄 리뷰 삭제 (completed)", description = "한줄 리뷰를 삭제합니다.")
    @ApiResponse( responseCode = "204", description = "한줄리뷰 삭제 성공")
    @ApiErrorResponses({ErrorCode.INVALID_STORE_UUID, ErrorCode.STORE_REVIEW_SERVICE_ERROR, ErrorCode.STORE_REVIEW_NOT_FOUND, ErrorCode.INVALID_STORE_REVIEW, ErrorCode.STORE_REVIEW_DELETE_FAILED})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @DeleteMapping("/{reviewUuid}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID storeUuid, @PathVariable UUID reviewUuid) {
        storeReviewService.deleteReview(storeUuid, reviewUuid);
        return ResponseEntity.noContent().build();
    }
}
