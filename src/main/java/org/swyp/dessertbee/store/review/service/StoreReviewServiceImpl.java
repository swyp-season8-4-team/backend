package org.swyp.dessertbee.store.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.dto.ReportRequest;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.entity.ReportCategory;
import org.swyp.dessertbee.common.repository.ReportRepository;
import org.swyp.dessertbee.statistics.store.entity.enums.ReviewAction;
import org.swyp.dessertbee.statistics.store.event.StoreReviewActionEvent;
import org.swyp.dessertbee.statistics.store.repostiory.StoreStatisticsRepository;
import org.swyp.dessertbee.store.review.dto.response.UserReviewListResponse;
import org.swyp.dessertbee.store.review.entity.StoreReviewReport;
import org.swyp.dessertbee.store.review.repository.StoreReviewReportRepository;
import org.swyp.dessertbee.store.store.entity.Store;
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
import org.swyp.dessertbee.user.service.UserService;

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
    private final StoreStatisticsRepository storeStatisticsRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;
    private final StoreReviewReportRepository storeReviewReportRepository;
    private final ReportRepository reportRepository;

    /** 오늘 작성한 리뷰 여부 확인 */
    public boolean hasTodayReview(UUID storeUuid, UUID userUuid) {
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);

            if (storeId == null) {
                throw new InvalidStoreUuidException();
            }

            int todayReviewCount = storeReviewRepository.countTodayReviewsByUserAndStore(userUuid, storeId);
            return todayReviewCount > 0;
        } catch (Exception e) {
            log.error("오늘 작성한 리뷰 여부 조회 처리 중 오류 발생", e);
            throw new StoreReviewServiceException("오늘 작성한 리뷰 여부 조회 처리 중 오류가 발생했습니다.");
        }
    }

    /** 리뷰 등록 */
    @Override
    public StoreReviewResponse createReview(UUID storeUuid, StoreReviewCreateRequest request, List<MultipartFile> images) {
        try{
            Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
            if (storeId == null) {
                throw new InvalidStoreUuidException();
            }

            // 오늘 같은 가게에 이미 작성했는지 체크
            int todayReviewCount = storeReviewRepository.countTodayReviewsByUserAndStore(request.getUserUuid(), storeId);
            if (todayReviewCount > 0) {
                throw new StoreReviewAlreadyExistsTodayException();
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

            eventPublisher.publishEvent(
                    new StoreReviewActionEvent(
                            review.getStoreId(),
                            review.getReviewId(),
                            reviewer.getUserUuid(),
                            ReviewAction.CREATE
                    )
            );

            storeStatisticsRepository.increaseStoreReviewCount(storeId);

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
    public List<StoreReviewResponse> getReviewsByStoreUuid(UUID storeUuid) {
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

            eventPublisher.publishEvent(
                    new StoreReviewActionEvent(
                            review.getStoreId(),
                            review.getReviewId(),
                            review.getUserUuid(),
                            ReviewAction.DELETE
                    )
            );

            storeStatisticsRepository.decreaseStoreReviewCount(storeId);
        } catch (StoreReviewDeleteFailedException e){
            log.warn("가게 한줄리뷰 삭제 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("가게 한줄리뷰 삭제 처리 중 오류 발생", e);
            throw new StoreReviewServiceException("가게 한줄리뷰 삭제 처리 중 오류가 발생했습니다.");
        }
    }

    /** 유저가 작성한 한줄 리뷰 리스트 (최신 등록순) 조회 */
    public UserReviewListResponse getUserReviewList() {
        try {
            UserEntity user = userService.getCurrentUser();
            List<StoreReview> reviews = storeReviewRepository.findByUserUuidOrderByCreatedAtDesc(user.getUserUuid());

            List<UserReviewListResponse.UserReviewItem> items = reviews.stream().map(review -> {
                Store store = storeRepository.findById(review.getStoreId())
                        .orElseThrow(StoreNotFoundException::new);

                // 썸네일은 대표 이미지 리스트 중 첫 번째
                List<String> storeImages = imageService.getImagesByTypeAndId(ImageType.STORE, store.getStoreId());
                String thumbnail = storeImages.isEmpty() ? null : storeImages.get(0);

                // 리뷰 이미지도 첫 번째만
                List<String> reviewImages = imageService.getImagesByTypeAndId(ImageType.SHORT, review.getReviewId());
                String reviewImage = reviewImages.isEmpty() ? null : reviewImages.get(0);

                return UserReviewListResponse.UserReviewItem.builder()
                        .reviewUuid(review.getReviewUuid())
                        .reviewImage(reviewImage)
                        .rating(review.getRating())
                        .content(review.getContent())
                        .createdAt(review.getCreatedAt())
                        .store(UserReviewListResponse.UserReviewItem.StoreInfo.builder()
                                .storeUuid(store.getStoreUuid())
                                .name(store.getName())
                                .address(store.getAddress())
                                .thumbnail(thumbnail)
                                .build())
                        .build();
            }).toList();

            return UserReviewListResponse.builder()
                    .reviewCount(items.size())
                    .reviews(items)
                    .build();

        } catch (Exception e) {
            log.error("유저 리뷰 리스트 조회 중 오류 발생", e);
            throw new StoreReviewServiceException("유저 리뷰 리스트 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void reportReview(UUID reviewUuid, ReportRequest request) {

        UserEntity user = userService.getCurrentUser();

        StoreReview review =  storeReviewRepository.findByReviewUuid(reviewUuid)
                .orElseThrow(() -> new StoreReviewNotFoundException());

        StoreReviewReport report = storeReviewReportRepository.findByReviewIdAndUserId(review.getReviewId(), user.getId());


        if(report != null){
            throw new DuplicationReportException();
        }

        // 6L로 타입 일치
        // '기타' 신고인 경우 사용자가 입력한 코멘트를 그대로 저장
        if (request.getReportCategoryId().equals(6L)){
            storeReviewReportRepository.save(
                    StoreReviewReport.builder()
                            .reportCategoryId(request.getReportCategoryId())
                            .reviewId(review.getReviewId())
                            .userId(user.getId())
                            .comment(request.getReportComment())
                            .build()
            );

            return;
        }

        //신고 유형 코멘트 조회
        ReportCategory reportCategory = reportRepository.findByReportCategoryId(request.getReportCategoryId());

        // '기타'가 아닌 경우 미리 정의된 신고 유형 코멘트 조회 후 저장
        storeReviewReportRepository.save(
                StoreReviewReport.builder()
                        .reportCategoryId(request.getReportCategoryId())
                        .reviewId(review.getReviewId())
                        .userId(user.getId())
                        .comment(reportCategory.getReportComment())
                        .build()
        );


    }
}
