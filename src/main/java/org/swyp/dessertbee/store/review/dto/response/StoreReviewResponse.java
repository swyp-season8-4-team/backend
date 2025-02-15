package org.swyp.dessertbee.store.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.review.entity.StoreReview;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class StoreReviewResponse {
    private UUID reviewUuid;
    private Long storeId;
    private String content;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private List<String> images;

    public static StoreReviewResponse fromEntity(StoreReview review, List<String> images) {
        if (review.getReviewUuid() == null) {
            throw new IllegalStateException("reviewUuid가 null입니다. 리뷰가 정상적으로 저장되었는지 확인해주세요.");
        }

        return StoreReviewResponse.builder()
                .reviewUuid(review.getReviewUuid())
                .storeId(review.getStoreId())
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .images(images)
                .build();
    }
}
