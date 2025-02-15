package org.swyp.dessertbee.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.entity.StoreReview;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class StoreReviewResponse {
    private Long id;
    private Long storeId; // 가게 ID 추가
    private String content;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private List<String> images;

    public static StoreReviewResponse fromEntity(StoreReview review, List<String> images) {
        return StoreReviewResponse.builder()
                .id(review.getId())
                .storeId(review.getStoreId()) // 가게 ID 추가
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .images(images)
                .build();
    }
}
