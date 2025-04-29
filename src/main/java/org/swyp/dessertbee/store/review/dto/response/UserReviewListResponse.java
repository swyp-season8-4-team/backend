package org.swyp.dessertbee.store.review.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserReviewListResponse {

    private int reviewCount;
    private List<UserReviewItem> reviews;

    @Data
    @Builder
    public static class UserReviewItem {
        private UUID reviewUuid;
        private String reviewImage;
        private BigDecimal rate;
        private String content;
        private LocalDateTime createdAt;
        private StoreInfo store;

        @Data
        @Builder
        public static class StoreInfo {
            private UUID storeUuid;
            private String thumbnail;
            private String name;
            private String address;
        }
    }
}
