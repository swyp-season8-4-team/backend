package org.swyp.dessertbee.store.review.service;

import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.dto.ReportRequest;
import org.swyp.dessertbee.store.review.dto.request.StoreReviewCreateRequest;
import org.swyp.dessertbee.store.review.dto.request.StoreReviewUpdateRequest;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.review.dto.response.UserReviewListResponse;

import java.util.List;
import java.util.UUID;

public interface StoreReviewService {

    /** 한 유저가 한 가게에 오늘 날짜로 등록한 리뷰의 존재 여부 확인 */
    boolean hasTodayReview(UUID storeUuid, UUID userUuid);

    /** 리뷰 등록 */
    StoreReviewResponse createReview(UUID storeUuid, StoreReviewCreateRequest request, List<MultipartFile> images);

    /** 특정 가게 리뷰 조회 */
    List<StoreReviewResponse> getReviewsByStoreUuid(UUID storeUuid);

    /** 리뷰 수정 */
    StoreReviewResponse updateReview(UUID storeUuid, UUID reviewUuid, StoreReviewUpdateRequest request, List<MultipartFile> newImages);

    /** 리뷰 삭제 */
    void deleteReview(UUID storeUuid, UUID reviewUuid);

    /** 유저가 작성한 한줄 리뷰 리스트 (최신 등록순) 조회 */
    UserReviewListResponse getUserReviewList();

    /** 리뷰 신고 */
    void reportReview(UUID reviewUuid, ReportRequest request);
}
