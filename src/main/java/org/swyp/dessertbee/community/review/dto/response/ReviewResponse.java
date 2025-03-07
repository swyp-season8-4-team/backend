package org.swyp.dessertbee.community.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.community.review.dto.ReviewContentDto;
import org.swyp.dessertbee.community.review.dto.ReviewPlace;
import org.swyp.dessertbee.community.review.entity.Review;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ReviewResponse {

    private UUID reviewUuid;
    private Long storeId;
    private UUID userUuid;
    private String nickname;
    private String profileImage;
    // 새롭게 추가된 필드: 리뷰 콘텐츠 배열 (각 요소는 text 또는 image 타입의 정보를 담습니다)
    private List<ReviewContentDto> contents;
    private ReviewPlace place;
    private String reviewCategory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean saved;
    private UserEntity.Gender gender;

    public static ReviewResponse fromEntity(UserEntity user,
                                            Review review,
                                            List<ReviewContentDto> contents,
                                            String reviewCategory,
                                            String profileImage,
                                            boolean saved) {

        return ReviewResponse.builder()
                .reviewUuid(review.getReviewUuid())
                .storeId(review.getStoreId())
                .userUuid(user.getUserUuid())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .profileImage(profileImage)
                .contents(contents)
                .reviewCategory(reviewCategory)
                .place(ReviewPlace.builder()
                        .placeName(review.getPlaceName())
                        .longitude(review.getLongitude())
                        .latitude(review.getLatitude())
                        .address(review.getAddress())
                        .build())
                .saved(saved)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
