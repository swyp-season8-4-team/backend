package org.swyp.dessertbee.store.review.service;

import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.review.dto.request.StoreReviewCreateRequest;
import org.swyp.dessertbee.store.review.dto.request.StoreReviewUpdateRequest;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;

import java.util.List;
import java.util.UUID;

public interface StoreReviewService {
    /** 리뷰 등록 */
    StoreReviewResponse createReview(UUID storeUuid, StoreReviewCreateRequest request, List<MultipartFile> images);

    /** 특정 가게 리뷰 조회 */
    List<StoreReviewResponse> getReviewsByStoreId(UUID storeUuid);

    /** 리뷰 수정 */
    StoreReviewResponse updateReview(UUID storeUuid, UUID reviewUuid, StoreReviewUpdateRequest request, List<MultipartFile> newImages);

    /** 리뷰 삭제 */
    void deleteReview(UUID storeUuid, UUID reviewUuid);
}
