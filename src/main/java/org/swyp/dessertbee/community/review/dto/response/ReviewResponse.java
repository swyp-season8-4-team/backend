package org.swyp.dessertbee.community.review.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.community.review.dto.ReviewPlace;
import org.swyp.dessertbee.community.review.entity.Review;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
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
    private String content;
    private String title;
    private List<String> reviewImages;
    private ReviewPlace place;
    private String reviewCategory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean saved;
    private UserEntity.Gender gender;

    public static ReviewResponse fromEntity(UserEntity user,
                                                     Review review,
                                                     List<String> reivewImages,
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
                .title(review.getTitle())
                .content(review.getContent())
                .reviewImages(reivewImages)
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
