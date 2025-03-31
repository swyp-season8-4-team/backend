package org.swyp.dessertbee.store.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.store.store.exception.StoreExceptions.*;
import org.swyp.dessertbee.store.review.exception.StoreReviewExceptions.*;
import org.swyp.dessertbee.store.store.service.StoreService;
import org.swyp.dessertbee.user.exception.UserExceptions.*;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.review.dto.request.StoreReviewCreateRequest;
import org.swyp.dessertbee.store.review.dto.request.StoreReviewUpdateRequest;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.review.entity.StoreReview;
import org.swyp.dessertbee.store.review.repository.StoreReviewRepository;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreReviewServiceImpl implements StoreReviewService {

    private final StoreReviewRepository storeReviewRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final StoreService storeService;

    /** 리뷰 등록 */
    @Override
    public StoreReviewResponse createReview(UUID storeUuid, StoreReviewCreateRequest request, List<MultipartFile> images) {
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            if (storeId == null) {
                throw new InvalidStoreUuidException();
            }

            StoreReview review = StoreReview.builder()
                    .storeId(storeId)
                    .userUuid(request.getUserUuid())
                    .content(request.getContent())
                    .rating(request.getRating())
                    .build();

            storeReviewRepository.save(review);

            if (review.getReviewUuid() == null) {
                throw new InvalidStoreReviewUuidException();
            }

            if (images != null && !images.isEmpty()) {
                imageService.uploadAndSaveImages(images, ImageType.SHORT, review.getReviewId(), "short/" + review.getReviewId());
            }

            List<String> imageUrls = imageService.getImagesByTypeAndId(ImageType.SHORT, review.getReviewId());

            Long userId = userRepository.findIdByUserUuid(request.getUserUuid());
            UserEntity reviewer = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException());
            List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, reviewer.getId());

            // 리뷰 등록 후 평균 평점 업데이트
            storeService.updateAverageRating(storeId);

            return StoreReviewResponse.fromEntity(review, reviewer,
                    profileImage.isEmpty() ? null : profileImage.get(0), imageUrls);
        } catch (StoreReviewCreationFailedException e){
            log.warn("가게 한줄리뷰 등록 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 한줄리뷰 등록 처리 중 오류 발생", e);
            throw new StoreReviewServiceException("가게 한줄리뷰 등록 처리 중 오류가 발생했습니다.");
        }
    }

    /** 특정 가게 리뷰 조회 */
    @Override
    public List<StoreReviewResponse> getReviewsByStoreId(UUID storeUuid) {
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            if (storeId == null) {
                throw new InvalidStoreUuidException();
            }
            List<StoreReview> reviews = storeReviewRepository.findByStoreIdAndDeletedAtIsNull(storeId);

            return reviews.stream()
                    .map(review -> {
                        List<String> images = imageService.getImagesByTypeAndId(ImageType.SHORT, review.getReviewId());

                        Long userId = userRepository.findIdByUserUuid(review.getUserUuid());
                        UserEntity reviewer = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException());
                        List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, reviewer.getId());

                        return StoreReviewResponse.fromEntity(review, reviewer,
                                profileImage.isEmpty() ? null : profileImage.get(0), images);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("가게 한줄리뷰 조회 처리 중 오류 발생", e);
            throw new StoreReviewServiceException("가게 한줄리뷰 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 리뷰 수정 */
    @Override
    public StoreReviewResponse updateReview(UUID storeUuid, UUID reviewUuid, StoreReviewUpdateRequest request, List<MultipartFile> newImages) {
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Long reviewId = storeReviewRepository.findReviewIdByReviewUuid(reviewUuid);
            StoreReview review = storeReviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)
                    .orElseThrow(() -> new StoreReviewNotFoundException());

            if (!review.getStoreId().equals(storeId)) {
                throw new InvalidStoreReviewException();
            }

            review.updateContentAndRating(request.getContent(), request.getRating());

            // 이미지 처리
            if (newImages != null && !newImages.isEmpty()) {
                imageService.deleteImagesByRefId(ImageType.SHORT, reviewId);
                imageService.uploadAndSaveImages(newImages, ImageType.SHORT, reviewId, "short/" + review.getReviewId());
            }

            List<String> updatedImages = imageService.getImagesByTypeAndId(ImageType.SHORT, reviewId);

            Long userId = userRepository.findIdByUserUuid(review.getUserUuid());
            UserEntity reviewer = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException());
            List<String> profileImage = imageService.getImagesByTypeAndId(ImageType.PROFILE, reviewer.getId());

            storeService.updateAverageRating(storeId);

            return StoreReviewResponse.fromEntity(review, reviewer,
                    profileImage.isEmpty() ? null : profileImage.get(0), updatedImages);
        } catch (StoreReviewUpdateFailedException e){
            log.warn("가게 한줄리뷰 수정 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 한줄리뷰 수정 처리 중 오류 발생", e);
            throw new StoreReviewServiceException("가게 한줄리뷰 수정 처리 중 오류가 발생했습니다.");
        }
    }


    /** 리뷰 삭제 */
    @Override
    public void deleteReview(UUID storeUuid, UUID reviewUuid) {
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            Long reviewId = storeReviewRepository.findReviewIdByReviewUuid(reviewUuid);
            StoreReview review = storeReviewRepository.findByReviewIdAndDeletedAtIsNull(reviewId)
                    .orElseThrow(() -> new StoreReviewNotFoundException());

            if (!review.getStoreId().equals(storeId)) {
                throw new InvalidStoreReviewException();
            }

            review.softDelete();
            storeService.updateAverageRating(storeId);
        } catch (StoreReviewDeleteFailedException e){
            log.warn("가게 한줄리뷰 삭제 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 한줄리뷰 삭제 처리 중 오류 발생", e);
            throw new StoreReviewServiceException("가게 한줄리뷰 삭제 처리 중 오류가 발생했습니다.");
        }
    }
}
