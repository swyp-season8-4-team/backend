package org.swyp.dessertbee.store.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.review.entity.StoreReview;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class StoreReviewResponse {
    @Schema(description = "리뷰 UUID", example = "4e8e1e28-c94e-40d7-8e93-6789abc45678")
    @NotNull(message = "리뷰 UUID는 필수입니다.")
    private UUID reviewUuid;

    @Schema(description = "가게 식별자 (PK)", example = "1")
    @NotNull(message = "가게 식별자는 필수입니다.")
    private Long storeId;

    @Schema(description = "사용자 UUID", example = "4e8e1e28-c94e-40d7-8e93-6789abc45678")
    @NotNull(message = "사용자 UUID는 필수입니다.")
    private UUID userUuid;

    @Schema(description = "작성자 이름", example = "이예림")
    @NotBlank(message = "작성자 이름은 필수입니다.")
    private String nickname;

    @Schema(description = "작성자 성별", example = "FEMALE")
    @NotBlank(message = "작성자 성별은 필수입니다.")
    private UserEntity.Gender gender;

    @Schema(description = "사용자 프로필 사진 URL", nullable = true)
    private String profileImage;

    @Schema(description = "리뷰 내용", example = "맛있음 어쩌구 저쩌구")
    @NotBlank(message = "리뷰 내용은 필수입니다.")
    private String content;

    @Schema(description = "평점", example = "4.0")
    @NotNull(message = "평점은 필수입니다.")
    @DecimalMin(value = "0.0", message = "평점은 0.0 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
    private BigDecimal rating;

    @Schema(description = "리뷰 작성 시간", example = "2025-04-03T14:30:00")
    @NotNull(message = "리뷰 작성 시간은 필수입니다.")
    private LocalDateTime createdAt;

    @Schema(description = "리뷰 이미지 URL 리스트", nullable = true)
    private List<String> images;

    public static StoreReviewResponse fromEntity(StoreReview review, UserEntity reviewer, String profileImage, List<String> images) {
        if (review.getReviewUuid() == null) {
            throw new IllegalStateException("reviewUuid가 null입니다. 리뷰가 정상적으로 저장되었는지 확인해주세요.");
        }

        return StoreReviewResponse.builder()
                .reviewUuid(review.getReviewUuid())
                .storeId(review.getStoreId())
                .userUuid(reviewer.getUserUuid())
                .nickname(reviewer.getNickname())
                .gender(reviewer.getGender())
                .profileImage(profileImage)
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .images(images)
                .build();
    }
}
