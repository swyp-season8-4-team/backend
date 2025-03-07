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
import org.swyp.dessertbee.community.review.dto.ReviewImage;
import org.swyp.dessertbee.community.review.dto.request.ReviewCreateRequest;
import org.swyp.dessertbee.community.review.dto.request.ReviewUpdateRequest;
import org.swyp.dessertbee.community.review.dto.response.ReviewPageResponse;
import org.swyp.dessertbee.community.review.dto.response.ReviewResponse;
import org.swyp.dessertbee.community.review.entity.Review;
import org.swyp.dessertbee.community.review.entity.SavedReview;
import org.swyp.dessertbee.community.review.exception.ReviewException.*;
import org.swyp.dessertbee.community.review.repository.ReviewRepository;
import org.swyp.dessertbee.community.review.repository.SavedReviewRepository;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserServiceImpl;

import java.util.List;
import java.util.UUID;
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


    /**
     * 커뮤니티 리뷰 생성
     * */
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, List<MultipartFile> reviewImages) {

        Long userId = userRepository.findIdByUserUuid(request.getUserUuid());

        if (userId == null) {
            throw new UserNotFoundExcption("존재하지 않는 유저입니다.");
        }

        //장소명으로 storeId 조회
        Long storeId = storeRepository.findStoreIdByName(request.getPlace().getPlaceName());

        Review review = reviewRepository.save(
                Review.builder()
                        .userId(userId)
                        .storeId(storeId)
                        .reviewCategoryId(request.getReviewCategoryId())
                        .title(request.getTitle())
                        .content(request.getContent())
                        .placeName(request.getPlace().getPlaceName())
                        .build()
        );


        //기존 이미지 삭제 후 새 이미지 업로드
        if (reviewImages != null && !reviewImages.isEmpty()) {
            String folder = "review/" + review.getReviewId();
            imageService.uploadAndSaveImages(reviewImages, ImageType.REVIEW, review.getReviewId(), folder);
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

        System.out.println(reviewImages);
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

        try {
            review.softDelete();
            reviewRepository.save(review);


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

        List<Image> images = imageRepository.findIdAndUrlByRefTypeAndRefId(ImageType.REVIEW, review.getReviewId());
        List<ReviewImage> reviewImages = images.stream()
                .map(image -> ReviewImage.builder()
                        .reviewImages(image.getUrl())     // URL을 reviewImage에 매핑
                        .reviewImageId(image.getId())      // id를 reviewImageId에 매핑
                        .build())
                .collect(Collectors.toList());


        //mateCategoryId로 name 조회
        String reviewCategory = String.valueOf(reviewRepository.findNameByReviewCategoryId( review.getReviewCategoryId()));


        //작성자 UUID 조회
        UserEntity user = userRepository.findById(review.getUserId())
                .orElseThrow(() -> new UserNotFoundExcption("존재하지 않는 유저입니다."));

        //작성자 프로필 조회
        String profileImage = imageService.getImageByTypeAndId(ImageType.PROFILE, review.getReviewId());

        SavedReview savedReview = (currentUserId != null)
                ? savedReviewRepository.findBySavedReviewIdAndUserId(review.getReviewId(), currentUserId)
                : null;
        boolean saved = savedReview != null;

        return ReviewResponse.fromEntity(user, review, reviewImages, reviewCategory, profileImage, saved);

    }

}
