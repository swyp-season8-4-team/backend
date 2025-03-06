package org.swyp.dessertbee.community.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.mate.exception.MateExceptions.*;
import org.swyp.dessertbee.community.review.dto.request.ReviewCreateRequest;
import org.swyp.dessertbee.community.review.dto.response.ReviewResponse;
import org.swyp.dessertbee.community.review.entity.Review;
import org.swyp.dessertbee.community.review.entity.SavedReview;
import org.swyp.dessertbee.community.review.exception.ReviewException.*;
import org.swyp.dessertbee.community.review.repository.ReviewRepository;
import org.swyp.dessertbee.community.review.repository.SavedReviewRepository;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserServiceImpl;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ReviewService {

    private final UserServiceImpl userService;
    private final StoreRepository storeRepository;
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final SavedReviewRepository savedReviewRepository;


    /**
     * 커뮤니티 리뷰 생성
     * */
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
            imageService.uploadAndSaveImages(reviewImages, ImageType.REVIEW, review.getReviewCategoryId(), folder);
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
     * 커뮤니티 리뷰 정보 조회 중복 코드
     * */
    private ReviewResponse mapToReviewDetailResponse(Review review, Long currentUserId) {

        List<String> reviewImages = imageService.getImagesByTypeAndId(ImageType.REVIEW, review.getReviewId());
        //mateCategoryId로 name 조회
        String reviewCategory = String.valueOf(reviewRepository.findNameByReviewCategoryId( review.getReviewCategoryId()));


        //작성자 UUID 조회
        UserEntity user = userRepository.findById(review.getUserId())
                .orElseThrow(() -> new UserNotFoundExcption("존재하지 않는 유저입니다."));

        //작성자 프로필 조회
        String profileImage = imageService.getImageByTypeAndId(ImageType.PROFILE, review.getUserId());

        SavedReview savedReview = (currentUserId != null)
                ? savedReviewRepository.findBySavedReviewIdAndUserId(review.getReviewId(), currentUserId)
                : null;
        boolean saved = savedReview != null;

        return ReviewResponse.fromEntity(user, review, reviewImages, reviewCategory, profileImage, saved);

    }
}
