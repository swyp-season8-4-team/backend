package org.swyp.dessertbee.community.review.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.Image;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.repository.ImageRepository;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.community.review.dto.ReviewContentDto;
import org.swyp.dessertbee.community.review.dto.ReviewImage;
import org.swyp.dessertbee.community.review.dto.request.ReviewCreateRequest;
import org.swyp.dessertbee.community.review.dto.request.ReviewUpdateRequest;
import org.swyp.dessertbee.community.review.dto.response.ReviewPageResponse;
import org.swyp.dessertbee.community.review.dto.response.ReviewResponse;
import org.swyp.dessertbee.community.review.entity.Review;
import org.swyp.dessertbee.community.review.entity.ReviewContent;
import org.swyp.dessertbee.community.review.entity.SavedReview;
import org.swyp.dessertbee.community.review.exception.ReviewException.*;
import org.swyp.dessertbee.community.review.repository.ReviewContentRepository;
import org.swyp.dessertbee.community.review.repository.ReviewRepository;
import org.swyp.dessertbee.community.review.repository.SavedReviewRepository;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserServiceImpl;

import java.util.*;
import java.util.stream.Collectors;


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


    /**
     * 커뮤니티 리뷰 생성
     * */
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, List<MultipartFile> reviewImages) {

        Long userId = userRepository.findIdByUserUuid(request.getUserUuid());
        if (userId == null) {
            throw new UserNotFoundExcption("존재하지 않는 유저입니다.");
        }

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


        // 이미지 타입 콘텐츠들 중 최대 imageIndex 계산
        int maxImageIndex = request.getContents().stream()
                .filter(content -> "image".equals(content.getType()))
                .mapToInt(ReviewContentDto::getImageIndex)
                .max()
                .orElse(-1);

        // 이미지 콘텐츠가 존재한다면, 제공된 이미지 파일 수와 인덱스 일치를 검증합니다.
        if (maxImageIndex != -1) {
            // 이미지의 총 갯수는 최대 인덱스 + 1이어야 함
            if (reviewImages == null || reviewImages.size() != (maxImageIndex + 1)) {
                throw new ImageIndexNotFoundException("요청된 이미지 인덱스가 이미지 파일 수보다 적습니다.");
            }

            // 리뷰 컨텐츠 배열 처리
            // request.getContents()는 텍스트와 이미지 타입을 모두 포함하는 리스트입니다.
            for (ReviewContentDto contentRequest : request.getContents()) {
                if ("text".equals(contentRequest.getType())) {
                    // 텍스트 컨텐츠 저장
                    ReviewContent reviewContent = ReviewContent.builder()
                            .reviewId(review.getReviewId())
                            .type("text")
                            .value(contentRequest.getValue())
                            .build();
                    reviewContentRepository.save(reviewContent);
                } else if ("image".equals(contentRequest.getType())) {
                    // 이미지 컨텐츠의 경우, imageIndex를 활용하여 해당 MultipartFile 선택
                    int idx = contentRequest.getImageIndex();
                    if (idx < reviewImages.size()) {
                        MultipartFile imageFile = reviewImages.get(idx);
                        String folder = "review/" + review.getReviewId();
                        // 단건 이미지 업로드 메서드 (imageIndex도 함께 저장)
                        Image image = imageService.uploadAndSaveImages(imageFile, ImageType.REVIEW, review.getReviewId(), folder, idx);

                        // 업로드된 이미지의 URL(또는 파일명)을 컨텐츠 값으로 저장
                        ReviewContent reviewContent = ReviewContent.builder()
                                .reviewId(review.getReviewId())
                                .type("image")
                                .value(image.getUrl()) // 필요에 따라 image.getFileName() 등으로 변경 가능
                                .imageIndex(idx)
                                .build();
                        reviewContentRepository.save(reviewContent);
                    }
                }
            }
        }

        return getReviewDetail(review.getReviewUuid());
    }

    /**
     * 커뮤니티 리뷰 상세 조회
     * */
    public ReviewResponse getReviewDetail(UUID reviewUuid) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        Review review = reviewRepository.findByReviewUuid(reviewUuid)
                .orElseThrow(() -> new ReviewNotFoundException("존재하지 않는 리뷰입니다."));

        // 현재 접속해 있는 사용자의 user 정보 (user가 null일 수 있으므로 null 체크)
        Long currentUserId = (user != null) ? user.getId() : null;


        return mapToReviewDetailResponse(review, currentUserId);
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
                .map( review -> mapToReviewDetailResponse(review, currentUserId))
                .collect(Collectors.toList());

        return new ReviewPageResponse(reviews, reviewsPage.isLast());
    }

    /**
     * 커뮤니티 리뷰 수정
     * */
    public void updateReview(UUID reviewUuid, ReviewUpdateRequest request, List<MultipartFile> reviewImages) {

        Review review = reviewRepository.findByReviewUuidAndDeletedAtIsNull(reviewUuid)
                .orElseThrow(() -> new ReviewNotFoundException("존재하지 않는 리뷰입니다."));

        Store store = storeRepository.findStoreIdByLongitudeAndLatitude(request.getPlace().getLongitude(), request.getPlace().getLatitude());

        review.update(request, store);



        if (reviewImages != null && !reviewImages.isEmpty()) {

            String folder = "review/" + review.getReviewId();
            imageService.updatePartialImages(request.getDeleteImageIds(), reviewImages, ImageType.REVIEW, review.getReviewId(), folder);
        }



    }

    /**
     * 커뮤니티 리뷰 삭제
     * */
    public void deleteReview(UUID reviewUuid) {
        Review review = reviewRepository.findByReviewUuidAndDeletedAtIsNull(reviewUuid)
                .orElseThrow(() -> new ReviewNotFoundException("존재하지 않는 리뷰입니다."));

        ReviewContent reviewContent = reviewContentRepository.findByReviewIdAndDeletedAtIsNull(review.getReviewId())
                .orElseThrow(() -> new ReviewNotFoundException("존재하지 않는 리뷰입니다."));
        try {
            review.softDelete();
            reviewContent.softDelete();
            reviewRepository.save(review);
            reviewContentRepository.save(reviewContent);

            imageService.deleteImagesByRefId(ImageType.REVIEW, review.getReviewId());

        } catch (Exception e)
        {
            System.out.println("❌ S3 이미지 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("S3 이미지 삭제 실패: " + e.getMessage(), e);
        }

    }


    /**
     * 커뮤니티 리뷰 정보 조회 중복 코드
     * */
    private ReviewResponse mapToReviewDetailResponse(Review review, Long currentUserId) {

        // review 테이블의 기본 정보는 그대로 사용하고,
        // review_content 테이블에서 해당 리뷰의 콘텐츠를 순서대로 조회합니다.
        List<ReviewContent> contents = reviewContentRepository.findByReviewIdOrderByImageIndexAsc(review.getReviewId());


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
                .orElseThrow(() -> new UserNotFoundExcption("존재하지 않는 유저입니다."));

        String profileImage = imageService.getImageByTypeAndId(ImageType.PROFILE, review.getReviewId());

        SavedReview savedReview = (currentUserId != null)
                ? savedReviewRepository.findBySavedReviewIdAndUserId(review.getReviewId(), currentUserId)
                : null;
        boolean saved = savedReview != null;

        // ReviewResponse에 contentList(리뷰 콘텐츠 배열)를 포함시킵니다.
        return ReviewResponse.fromEntity(user, review, contentList, reviewCategory, profileImage, saved);
    }


}
