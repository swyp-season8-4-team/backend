package org.swyp.dessertbee.store.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@Schema(description = "유저가 작성한 리뷰 리스트 응답")
public class UserReviewListResponse {

    @Schema(description = "유저가 작성한 전체 리뷰 개수", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private int reviewCount;

    @Schema(description = "유저가 작성한 리뷰 리스트", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<UserReviewItem> reviews;

    @Data
    @Builder
    @Schema(description = "한 개의 유저 리뷰 아이템")
    public static class UserReviewItem {

        @Schema(description = "리뷰 UUID", example = "4e8e1e28-c94e-40d7-8e93-6789abc45678", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "리뷰 UUID는 필수입니다.")
        private UUID reviewUuid;

        @Schema(description = "리뷰 이미지 URL (첫 번째 이미지, 없을 경우 null)", example = "reviewImage.jpg", nullable = true)
        private String reviewImage;

        @Schema(description = "평점", example = "4.0", minimum = "0.0", maximum = "5.0", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "평점은 필수입니다.")
        @DecimalMin(value = "0.0", message = "평점은 0.0 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
        private BigDecimal rating;

        @Schema(description = "한 줄 리뷰 내용 (최대 50자)", example = "정말 맛있었어요!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "리뷰 내용은 필수입니다.")
        private String content;

        @Schema(description = "리뷰 등록일시", example = "2025-04-03T14:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "리뷰 작성 시간은 필수입니다.")
        private LocalDateTime createdAt;

        @Schema(description = "리뷰를 작성한 가게 정보", requiredMode = Schema.RequiredMode.REQUIRED)
        private StoreInfo store;

        @Data
        @Builder
        @Schema(description = "간단한 가게 정보")
        public static class StoreInfo {

            @Schema(description = "가게 UUID", example = "9fa801ef-88f1-43ef-bda1-d1150c6fc12f", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "가게 UUID는 필수입니다.")
            private UUID storeUuid;

            @Schema(description = "가게 대표 이미지 URL (썸네일)", example = "storeImage.jpg", nullable = true)
            private String thumbnail;

            @Schema(description = "가게 이름", example = "디저트비 합정점", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "가게 이름은 필수입니다.")
            private String name;

            @Schema(description = "가게 주소", example = "서울 강남구 테헤란로 123", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "가게 주소는 필수입니다.")
            private String address;
        }
    }
}