package org.swyp.dessertbee.community.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.Image;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.repository.ImageRepository;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.review.dto.ReviewContentDto;
import org.swyp.dessertbee.community.review.dto.request.ReviewCreateRequest;
import org.swyp.dessertbee.community.review.dto.request.ReviewUpdateRequest;
import org.swyp.dessertbee.community.review.dto.response.ReviewPageResponse;
import org.swyp.dessertbee.community.review.dto.response.ReviewResponse;
import org.swyp.dessertbee.community.review.entity.Review;
import org.swyp.dessertbee.community.review.entity.ReviewContent;
import org.swyp.dessertbee.community.review.entity.ReviewStatistics;
import org.swyp.dessertbee.community.review.entity.SavedReview;
import org.swyp.dessertbee.community.review.repository.ReviewContentRepository;
import org.swyp.dessertbee.community.review.repository.ReviewRepository;
import org.swyp.dessertbee.community.review.repository.ReviewStatisticsRepository;
import org.swyp.dessertbee.community.review.repository.SavedReviewRepository;
import org.swyp.dessertbee.statistics.store.entity.enums.ReviewAction;
import org.swyp.dessertbee.statistics.store.event.CommunityReviewActionEvent;
import org.swyp.dessertbee.statistics.store.repostiory.StoreStatisticsRepository;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final UserServiceImpl userService;
    private final StoreRepository storeRepository;
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final SavedReviewRepository savedReviewRepository;
    private final ImageRepository imageRepository;
    private final ReviewContentRepository reviewContentRepository;
    private final ReviewStatisticsRepository reviewStatisticsRepository;
    private final StoreStatisticsRepository storeStatisticsRepository;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * 커뮤니티 리뷰 생성
     */
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, List<MultipartFile> reviewImages) {
        // 1. 사용자 확인
        Long userId = userRepository.findIdByUserUuid(request.getUserUuid());
        if (userId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userService.findById(userId);

        // 2. 장소명으로 storeId 조회 후 리뷰 저장
        Long storeId = storeRepository.findStoreIdByName(request.getPlace().getPlaceName());
        Review review = reviewRepository.save(
                Review.builder()
                        .userId(userId)
                        .storeId(storeId)
                        .reviewCategoryId(request.getReviewCategoryId())
                        .title(request.getTitle())
                        .placeName(request.getPlace().getPlaceName())
                        .latitude(request.getPlace().getLatitude())
                        .longitude(request.getPlace().getLongitude())
                        .address(request.getPlace().getAddress())
                        .build()
        );

        // 3. 콘텐츠 순회하여 리뷰 내용 저장 (텍스트와 이미지 처리)
        int imageCounter = 0;
        for (ReviewContentDto contentRequest : request.getContents()) {
            if ("text".equals(contentRequest.getType())) {
                ReviewContent reviewContent = ReviewContent.builder()
                        .reviewId(review.getReviewId())
                        .type("text")
                        .value(contentRequest.getValue())
                        .build();
                reviewContentRepository.save(reviewContent);
            } else if ("image".equals(contentRequest.getType())) {
                // 전달된 이미지 파일이 부족하면 예외 처리
                if (imageCounter >= reviewImages.size()) {
                    throw new BusinessException(ErrorCode.IMAGE_COUNT_MISMATCH);
                }

                // reviewImages에서 순차적으로 이미지를 하나씩 가져옵니다.
                MultipartFile reviewImage = reviewImages.get(imageCounter);

                String folder = "review/" + review.getReviewId();
                // imageUuid는 JSON 요청의 해당 필드 값을 사용합니다.
                Image image = imageService.reviewUploadAndSaveImage(reviewImage, ImageType.REVIEW, review.getReviewId(), folder);

                if (image == null) {
                    throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
                }
                ReviewContent reviewContent = ReviewContent.builder()
                        .reviewId(review.getReviewId())
                        .type("image")
                        .value(image.getUrl())
                        .imageUuid(contentRequest.getImageUuid())
                        .build();
                reviewContentRepository.save(reviewContent);
            }
        }

        // 4. 리뷰 통계 초기화
        // todo: 이제 불필요한것 같은데 이거 삭제해도되나요 현경님
        reviewStatisticsRepository.save(
                ReviewStatistics.builder()
                        .reviewId(review.getReviewId())
                        .views(0)
                        .saves(0)
                        .reviews(0)
                        .build()
        );

        UserEntity user = userService.getCurrentUser();

        eventPublisher.publishEvent(new CommunityReviewActionEvent(review.getStoreId(), review.getReviewId(), user.getUserUuid(), ReviewAction.CREATE));

        storeStatisticsRepository.increaseCommunityReviewCount(storeId);

        return getReviewDetail(review.getReviewUuid());
    }


    /**
     * 커뮤니티 리뷰 상세 조회
     */
    public ReviewResponse getReviewDetail(UUID reviewUuid) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        Review review = reviewRepository.findByReviewUuid(reviewUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_REVIEW_NOT_FOUND));

        // 현재 접속해 있는 사용자의 user 정보 (user가 null일 수 있으므로 null 체크)
        Long currentUserId = (user != null) ? user.getId() : null;

        ReviewStatistics reviewStatistics = reviewStatisticsRepository.findByReviewId(review.getReviewId());

        int views = reviewStatistics.getViews() + 1;
        reviewStatistics.count(views);

        return mapToReviewDetailResponse(review, currentUserId, views);
    }

    /**
     * 커뮤니티 리뷰 전체 조회
     */
    public ReviewPageResponse getReviews(Pageable pageable, String keyword, Long reviewCategoryId) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        Long currentUserId = (user != null) ? user.getId() : null;

        Page<Review> reviewsPage = reviewRepository.findByDeletedAtIsNullAndReviewCategoryId(pageable, keyword, reviewCategoryId);


        List<ReviewResponse> reviews = reviewsPage.stream()
                .map(review -> {
                    ReviewStatistics reviewStatistics = reviewStatisticsRepository.findByReviewId(review.getReviewId());
                    return mapToReviewDetailResponse(review, currentUserId, reviewStatistics.getViews());
                })
                .collect(Collectors.toList());


        return new ReviewPageResponse(reviews, reviewsPage.isLast());
    }

    /**
     * 커뮤니티 리뷰 수정
     */
    @Transactional
    public void updateReview(UUID reviewUuid, ReviewUpdateRequest request, List<MultipartFile> reviewImages) {

        // 1. 리뷰 조회 및 기본 정보 업데이트
        Review review = reviewRepository.findByReviewUuid(reviewUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_REVIEW_NOT_FOUND));

        Store store = storeRepository.findByName(request.getPlace().getPlaceName());
        review.update(request, store);


        // 3. 삭제할 이미지 ID 검증 및 삭제 처리
        List<Long> deleteIds = request.getDeleteImageIds();
        if (deleteIds != null && !deleteIds.isEmpty()) {
            if (deleteIds.size() != new HashSet<>(deleteIds).size()) {
                throw new BusinessException(ErrorCode.COMMUNITY_REVIEW_NOT_FOUND);
            }
            imageService.deleteImagesByIds(deleteIds);
        }
        // 4. 이미지 업로드 폴더 지정
        String folder = "review/" + review.getReviewId();

        // 5. 요청된 콘텐츠 저장 (텍스트 및 이미지)
        // reviewImages 리스트는 신규 업로드할 파일들만 포함한다고 가정합니다.
        int imageCounter = 0;
        int textCounter = 0;
        for (ReviewContentDto contentDto : request.getContents()) {
            if ("image".equals(contentDto.getType())) {
                // 클라이언트에서 전달한 imageUuid 및 기존 이미지 URL
                UUID providedUuid = contentDto.getImageUuid();
                String imageUrl = "";

                // providedUuid가 있으면 DB에서 기존 이미지 조회 시도
                if (providedUuid != null) {
                    ReviewContent reviewContent = reviewContentRepository.findByImageUuid(contentDto.getImageUuid());

                    if (reviewContent == null) {
                        // 기존 imageUuid가 있으나 DB에 없다면 신규 업로드 처리
                        if (reviewImages == null) {

                            if (imageCounter > reviewImages.size()) {
                                throw new BusinessException(ErrorCode.IMAGE_COUNT_MISMATCH);

                            }
                        }
                        MultipartFile file = reviewImages.get(imageCounter);

                        imageCounter++; // 신규 업로드 시에도 증가

                        Image uploadedImage = imageService.updatePartialImage(request.getDeleteImageIds(), file, ImageType.REVIEW, review.getReviewId(), folder);


                        if (uploadedImage == null) {
                            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
                        }

                        imageUrl = uploadedImage.getUrl();

                        providedUuid = contentDto.getImageUuid();


                        reviewContent = ReviewContent.builder()
                                .reviewId(review.getReviewId())
                                .type("image")
                                .value(imageUrl)
                                .imageUuid(providedUuid)
                                .build();
                        reviewContentRepository.save(reviewContent);
                    } else {
                        imageUrl = reviewContent.getValue();
                    }


                } else {
                    // 클라이언트에서 imageUuid를 제공하지 않으면 해당 콘텐츠는 무시(삽입하지 않음)
                    continue;
                }

            } else if ("text".equals(contentDto.getType())) {
                List<ReviewContent> existingTextContents = reviewContentRepository.findByReviewIdAndType(review.getReviewId(), "text");
                if (textCounter < existingTextContents.size()) {
                    // 기존 텍스트 콘텐츠가 있으면 업데이트
                    ReviewContent existingText = existingTextContents.get(textCounter);
                    existingText.update(contentDto.getValue());
                    reviewContentRepository.save(existingText);
                } else {
                    // 기존에 없는 경우 신규 등록
                    ReviewContent newTextContent = ReviewContent.builder()
                            .reviewId(review.getReviewId())
                            .type("text")
                            .value(contentDto.getValue())
                            .build();
                    reviewContentRepository.save(newTextContent);
                }
                textCounter++; // 순서에 따른 인덱스 증가
            }
        }
    }

    /**
     * 커뮤니티 리뷰 삭제
     * */
    public void deleteReview(UUID reviewUuid) {
        Review review = reviewRepository.findByReviewUuid(reviewUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_REVIEW_NOT_FOUND));

        List<ReviewContent> reviewContents = reviewContentRepository.findByReviewIdAndDeletedAtIsNull(review.getReviewId());
        if (reviewContents.isEmpty()) {
            throw new BusinessException(ErrorCode.COMMUNITY_REVIEW_NOT_FOUND);
        }
        try {
            review.softDelete();
            reviewRepository.save(review);
            for (ReviewContent reviewContent : reviewContents) {
                reviewContent.softDelete();
                reviewContentRepository.save(reviewContent);
            }

            imageService.deleteImagesByRefId(ImageType.REVIEW, review.getReviewId());

            UserEntity user = userService.getCurrentUser();
            eventPublisher.publishEvent(new CommunityReviewActionEvent(review.getStoreId(), review.getReviewId(), user.getUserUuid(), ReviewAction.DELETE));

            storeStatisticsRepository.decreaseCommunityReviewCount(review.getStoreId());
        } catch (Exception e)
        {
            log.error("❌ S3 이미지 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("S3 이미지 삭제 실패: " + e.getMessage(), e);
        }

    }

    /**
     * 커뮤니티 리뷰 정보 조회 중복 코드
     * */
    public ReviewResponse mapToReviewDetailResponse(Review review, Long currentUserId, int views) {
        // review 테이블의 기본 정보 사용
        List<ReviewContent> contents = reviewContentRepository.findByReviewIdOrderByReviewIdAsc(review.getReviewId());

        List<Image> images = imageRepository.findIdAndUrlByRefTypeAndRefId(ImageType.REVIEW, review.getReviewId());

        List<ReviewContentDto> contentList = contents.stream()
                .map(content -> {
                    ReviewContentDto dto = new ReviewContentDto();
                    dto.setType(content.getType());
                    if ("text".equals(content.getType())) {
                        dto.setValue(content.getValue());
                    } else if ("image".equals(content.getType())) {
                        dto.setImageUrl(content.getValue());
                        dto.setImageUuid(content.getImageUuid());
                        Optional<Image> matchingImage = images.stream()
                                .filter(image -> image.getUrl().equals(content.getValue()))
                                .findFirst();
                        dto.setImageId(matchingImage.map(Image::getId).orElse(null));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        // 리뷰 카테고리 및 작성자 정보 조회
        String reviewCategory = String.valueOf(reviewRepository.findNameByReviewCategoryId(review.getReviewCategoryId()));

        UserEntity user = userRepository.findById(review.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String profileImage = imageService.getImageByTypeAndId(ImageType.PROFILE, review.getReviewId());

        // ✅ 저장된 리뷰 확인하는 로직 수정
        boolean saved = false;
        if (currentUserId != null) {
            SavedReview savedReview = savedReviewRepository.findByReview_ReviewIdAndUserId(review.getReviewId(), currentUserId);
            saved = savedReview != null;
        }

        return ReviewResponse.fromEntity(user, review, contentList, reviewCategory, profileImage, views, saved);
    }


}
