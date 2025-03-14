package org.swyp.dessertbee.community.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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


    /**
     * 커뮤니티 리뷰 생성
     * */
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, List<MultipartFile> reviewImages) {

        Long userId = userRepository.findIdByUserUuid(request.getUserUuid());
        try {
            userService.findById(userId);


            // 장소명으로 storeId 조회
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

            // 이미지 타입 콘텐츠들 중 최대 imageIndex 계산 (이미지가 하나라도 있는 경우에만)
            int maxImageIndex = request.getContents().stream()
                    .filter(content -> "image".equals(content.getType()))
                    .mapToInt(ReviewContentDto::getImageIndex)
                    .max()
                    .orElse(-1);

            // 이미지 콘텐츠가 있다면, 제공된 이미지 파일 수와 인덱스 일치를 검증합니다.
            if (maxImageIndex != -1) {
                if (reviewImages == null || reviewImages.size() != (maxImageIndex + 1)) {
                    throw new BusinessException(ErrorCode.IMAGE_INDEX_NOT_FOUND);
                }
            }

            // 리뷰 콘텐츠 배열 처리 (텍스트와 이미지 모두 처리)
            for (ReviewContentDto contentRequest : request.getContents()) {
                if ("text".equals(contentRequest.getType())) {
                    ReviewContent reviewContent = ReviewContent.builder()
                            .reviewId(review.getReviewId())
                            .type("text")
                            .value(contentRequest.getValue())
                            .build();
                    reviewContentRepository.save(reviewContent);
                } else if ("image".equals(contentRequest.getType())) {
                    int idx = contentRequest.getImageIndex();
                    if (idx < reviewImages.size()) {
                        MultipartFile imageFile = reviewImages.get(idx);
                        String folder = "review/" + review.getReviewId();
                        Image image = imageService.uploadAndSaveImages(imageFile, ImageType.REVIEW, review.getReviewId(), folder, idx);
                        ReviewContent reviewContent = ReviewContent.builder()
                                .reviewId(review.getReviewId())
                                .type("image")
                                .value(image.getUrl())
                                .imageIndex(idx)
                                .build();
                        reviewContentRepository.save(reviewContent);
                    }
                }
            }

            //리뷰 통계 초기화
            reviewStatisticsRepository.save(
                    ReviewStatistics.builder()
                            .reviewId(review.getReviewId())
                            .views(0)
                            .saves(0)
                            .reviews(0)
                            .build()
            );

            return getReviewDetail(review.getReviewUuid());

        } catch (BusinessException e) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

    }


    /**
     * 커뮤니티 리뷰 상세 조회
     * */
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
     * */
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
     * */
    @Transactional
    public void updateReview(UUID reviewUuid, ReviewUpdateRequest request, List<MultipartFile> reviewImages) {

        // 리뷰 조회 및 기본 정보 업데이트
        Review review = reviewRepository.findByReviewUuid(reviewUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_REVIEW_NOT_FOUND));

        Store store = storeRepository.findByName(request.getPlace().getPlaceName());

        review.update(request, store);

        // 1. 리뷰 콘텐츠 업데이트 처리
        // 기존 콘텐츠 삭제 (또는 별도의 비교 로직을 수행)
        reviewContentRepository.deleteByReviewId(review.getReviewId());

        // 요청에 담긴 새로운 콘텐츠 배열을 저장합니다.
        // 이때 각 콘텐츠에는 contentOrder (또는 imageIndex) 정보가 들어있어야 합니다.
        for (ReviewContentDto contentDto : request.getContents()) {
            ReviewContent reviewContent = ReviewContent.builder()
                    .reviewId(review.getReviewId())
                    .type(contentDto.getType())
                    .value(contentDto.getValue())
                    .imageIndex(contentDto.getImageIndex()) // 이미지 콘텐츠일 경우 사용
                    .build();
            reviewContentRepository.save(reviewContent);
        }

        List<Long> deleteIds = request.getDeleteImageIds();
        if (deleteIds != null && !deleteIds.isEmpty()) {
            // 중복 체크: HashSet에 담아서 크기가 다르면 중복이 존재하는 것임
            if (deleteIds.size() != new HashSet<>(deleteIds).size()) {
                throw new BusinessException(ErrorCode.COMMUNITY_REVIEW_NOT_FOUND);
            }
        }

        // 2. 이미지 업데이트 처리
        // 이미지 삭제 후 새로운 이미지 추가
        if ((reviewImages != null && !reviewImages.isEmpty()) ||
                (request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty())) {
            String folder = "review/" + review.getReviewId();
            imageService.updatePartialImages(request.getDeleteImageIds(), reviewImages, ImageType.REVIEW, review.getReviewId(), folder);
        }

        // 3. 이미지 인덱스 재정렬 처리
        // 삭제 후 이미지 인덱스가 비연속적일 수 있으므로, DB에서 현재 이미지 목록을 새로 조회한 후 재정렬합니다.
        List<Image> images = imageRepository.findByRefTypeAndRefId(ImageType.REVIEW, review.getReviewId());

        // imageIndex가 null인 경우, Integer.MAX_VALUE로 간주하여 정렬 (null이 뒤로 오도록)
        images.sort(Comparator.comparing(img -> img.getImageIndex() == null ? Integer.MAX_VALUE : img.getImageIndex()));

        for (int newIndex = 0; newIndex < images.size(); newIndex++) {
            Image img = images.get(newIndex);
            // imageIndex가 null이거나, 현재 값이 재정렬된 값과 다르면 업데이트
            if (img.getImageIndex() == null || !img.getImageIndex().equals(newIndex)) {
                img.setImageIndex(newIndex);
                imageRepository.save(img);
            }
        }

        // 4. 리뷰 콘텐츠 내 이미지 인덱스 업데이트
        // review_content 테이블에 저장된 이미지 콘텐츠들도, 재정렬된 새로운 이미지 인덱스에 맞춰 업데이트
        List<ReviewContent> imageContents = reviewContentRepository.findByReviewIdAndType(review.getReviewId(), "image");
        for (ReviewContent rc : imageContents) {
            // 예를 들어, 삭제 전 rc.getImageIndex()가 였던 인덱스에 해당하는 새 인덱스를 찾는 로직
            // (여기서는 단순화를 위해, rc.getImageIndex()를 그대로 사용하지 않고, 실제 이미지 리스트에서 매칭)
            Optional<Image> matchingImage = images.stream()
                    .filter(img -> img.getImageIndex().equals(rc.getImageIndex()))
                    .findFirst();
            if (matchingImage.isPresent()) {
                // 만약 인덱스가 변경되었다면 업데이트합니다.
                Integer newImageIndex = matchingImage.get().getImageIndex();
                if (!rc.getImageIndex().equals(newImageIndex)) {
                    rc.setImageIndex(newImageIndex);
                    reviewContentRepository.save(rc);
                }
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

        } catch (Exception e)
        {
            log.error("❌ S3 이미지 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("S3 이미지 삭제 실패: " + e.getMessage(), e);
        }

    }


    /**
     * 커뮤니티 리뷰 정보 조회 중복 코드
     * */
    private ReviewResponse mapToReviewDetailResponse(Review review, Long currentUserId, int views) {

        // review 테이블의 기본 정보는 그대로 사용하고,
        // review_content 테이블에서 해당 리뷰의 콘텐츠를 순서대로 조회합니다.
        List<ReviewContent> contents = reviewContentRepository.findByReviewIdOrderByReviewIdAsc(review.getReviewId());


        List<Image> images = imageRepository.findIdAndUrlByRefTypeAndRefId(ImageType.REVIEW, review.getReviewId());

        List<ReviewContentDto> contentList = contents.stream()
                .map(content -> {
                    ReviewContentDto dto = new ReviewContentDto();
                    dto.setType(content.getType());
                    dto.setImageIndex(content.getImageIndex());
                    if ("text".equals(content.getType())) {
                        dto.setValue(content.getValue());
                    } else if ("image".equals(content.getType())) {
                        Optional<Image> matchingImage = images.stream()
                                .filter(img -> img.getImageIndex().equals(content.getImageIndex()))
                                .findFirst();
                        dto.setImageUrl(matchingImage.map(Image::getUrl).orElse(null));
                        dto.setImageId(matchingImage.map(Image::getId).orElse(null));
                    }
                    return dto;
                })
                .collect(Collectors.toList());



        // 나머지 메타데이터 처리 (예: 리뷰 카테고리, 작성자, 프로필 이미지 등)
        String reviewCategory = String.valueOf(reviewRepository.findNameByReviewCategoryId(review.getReviewCategoryId()));

        UserEntity user = userRepository.findById(review.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String profileImage = imageService.getImageByTypeAndId(ImageType.PROFILE, review.getReviewId());

        SavedReview savedReview = (currentUserId != null)
                ? savedReviewRepository.findBySavedReviewIdAndUserId(review.getReviewId(), currentUserId)
                : null;
        boolean saved = savedReview != null;

        // ReviewResponse에 contentList(리뷰 콘텐츠 배열)를 포함시킵니다.
        return ReviewResponse.fromEntity(user, review, contentList, reviewCategory, profileImage, views, saved);
    }


}
