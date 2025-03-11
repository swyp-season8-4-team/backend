package org.swyp.dessertbee.community.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.community.review.entity.Review;
import org.swyp.dessertbee.community.review.entity.ReviewReply;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ReviewReplyResponse {

    private UUID replyUuid;
    private UUID reviewUuid;
    private String nickname;
    private UUID userUuid;
    private String content;
    private String profileImage;
    private UserEntity.Gender gender;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static ReviewReplyResponse fromEntity(ReviewReply reply,
                                                 UUID reviewUuid,
                                                 UserEntity user,
                                                 String profileImage) {

        return ReviewReplyResponse.builder()
                .replyUuid(reply.getReviewReplyUuid())
                .reviewUuid(reviewUuid)
                .nickname(user.getNickname())
                .gender(user.getGender())
                .userUuid(user.getUserUuid())
                .content(reply.getContent())
                .profileImage(profileImage)
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();

    }

}
