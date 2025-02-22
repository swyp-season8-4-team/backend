package org.swyp.dessertbee.store.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.review.dto.request.StoreReviewCreateRequest;
import org.swyp.dessertbee.store.review.dto.request.StoreReviewUpdateRequest;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.review.entity.StoreReview;
import org.swyp.dessertbee.store.review.repository.StoreReviewRepository;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.store.store.service.StoreService;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreReviewService {

    private final StoreReviewRepository storeReviewRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final StoreService storeService;

    /** 리뷰 등록 */
    @Transactional
    public StoreReviewResponse createReview(UUID storeUuid, StoreReviewCreateRequest request, List<MultipartFile> images) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) {
            throw new BusinessException(ErrorCode.INVALID_STORE_UUID);
        }

        StoreReview review = StoreReview.builder()
                .storeId(storeId)
                .userId(request.getUserId())
                .content(request.getContent())
                .rating(request.getRating())
                .build();

        storeReviewRepository.save(review);

        if (review.getReviewUuid() == null) {
            throw new BusinessException(ErrorCode.INVALID_STORE_REVIEW_UUID);
        }

        if (images != null && !images.isEmpty()) {
            imageService.uploadAndSaveImages(images, ImageType.SHORT, review.getReviewId(), "short/" + review.getReviewId());
        }

        List<String> imageUrls = imageService.getImagesByTypeAndId(ImageType.SHORT, review.getReviewId());

        UserEntity reviewer = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, reviewer.getId());

        // 리뷰 등록 후 평균 평점 업데이트
        storeService.updateAverageRating(storeId);

        return StoreReviewResponse.fromEntity(review, reviewer.getNickname(),
                profileImage.isEmpty() ? null : profileImage.get(0), imageUrls);
    }

    /** 특정 가게 리뷰 조회 */
    @Transactional
    public List<StoreReviewResponse> getReviewsByStoreId(UUID storeUuid) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        List<StoreReview> reviews = storeReviewRepository.findByStoreIdAndDeletedAtIsNull(storeId);

        return reviews.stream()
                .map(review -> {
                    List<String> images = imageService.getImagesByTypeAndId(ImageType.SHORT, review.getReviewId());

                    UserEntity reviewer = userRepository.findById(review.getUserId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                    List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, reviewer.getId());

                    return StoreReviewResponse.fromEntity(review, reviewer.getNickname(),
                            profileImage.isEmpty() ? null : profileImage.get(0), images);
                })
                .collect(Collectors.toList());
    }

    /** 리뷰 수정 */
    @Transactional
    public StoreReviewResponse updateReview(UUID storeUuid, UUID reviewUuid, StoreReviewUpdateRequest request, List<MultipartFile> newImages) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        Long reviewId = storeReviewRepository.findReviewIdByReviewUuid(reviewUuid);
        StoreReview review = storeReviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_REVIEW_NOT_FOUND));

        if (!review.getStoreId().equals(storeId)) {
            throw new BusinessException(ErrorCode.INVALID_STORE_REVIEW);
        }

        review.setContent(request.getContent());
        review.setRating(request.getRating());

        // 이미지 처리
        imageService.deleteImagesByRefId(ImageType.SHORT, reviewId);
        if (newImages != null && !newImages.isEmpty()) {
            imageService.uploadAndSaveImages(newImages, ImageType.SHORT, reviewId, "short/" + review.getReviewId());
        }

        List<String> updatedImages = imageService.getImagesByTypeAndId(ImageType.SHORT, reviewId);

        UserEntity reviewer = userRepository.findById(review.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, reviewer.getId());

        storeService.updateAverageRating(storeId);

        return StoreReviewResponse.fromEntity(review, reviewer.getNickname(),
                profileImage.isEmpty() ? null : profileImage.get(0), updatedImages);
    }


    /** 리뷰 삭제 */
    @Transactional
    public void deleteReview(UUID storeUuid, UUID reviewUuid) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        Long reviewId = storeReviewRepository.findReviewIdByReviewUuid(reviewUuid);
        StoreReview review = storeReviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_REVIEW_NOT_FOUND));

        if (!review.getStoreId().equals(storeId)) {
            throw new BusinessException(ErrorCode.INVALID_STORE_REVIEW);
        }

        review.softDelete();
        storeService.updateAverageRating(storeId);
    }
}
