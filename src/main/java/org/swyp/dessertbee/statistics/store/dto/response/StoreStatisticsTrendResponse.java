package org.swyp.dessertbee.statistics.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.swyp.dessertbee.statistics.store.entity.StoreStatisticsTrend;
import org.swyp.dessertbee.statistics.store.entity.enums.PeriodType;

import java.math.BigDecimal;

@Builder
@Schema(description = "추이 통계 응답 DTO")
public record StoreStatisticsTrendResponse(
        @Schema(description = "추이 구간 (일간-DAILY: 2시간 단위 시간대, 주간-WEEKLY: 요일명, 월간-MONTHLY: N일)", example = "14:00~15:59")
        String displayKey,

        @Schema(description = "조회 수", example = "120")
        int viewCount,

        @Schema(description = "저장 수", example = "35")
        int saveCount,

        @Schema(description = "한줄 리뷰 수", example = "5")
        int reviewStoreCount,

        @Schema(description = "커뮤니티 리뷰 수", example = "2")
        int reviewCommCount,

        @Schema(description = "쿠폰 사용 수", example = "9")
        int couponUsedCount,

        @Schema(description = "디저트 메이트 모집글 수", example = "1")
        int mateCount,

        @Schema(description = "구간 기준 평균 평점", example = "4.25")
        BigDecimal averageRating,

        @Schema(description = "해당 구간의 총 리뷰 수", example = "7")
        int totalReviewCount
) {
        public static StoreStatisticsTrendResponse fromEntity(StoreStatisticsTrend trend, PeriodType periodType) {
                String displayKey = switch (periodType) {
                        case DAILY -> toTimeRange(trend.getDisplayKey());
                        case WEEKLY -> capitalize(trend.getDisplayKey());
                        case MONTHLY -> trend.getDisplayKey() + "일";
                };

                return StoreStatisticsTrendResponse.builder()
                        .displayKey(displayKey)
                        .viewCount(trend.getViewCount())
                        .saveCount(trend.getSaveCount())
                        .reviewStoreCount(trend.getReviewStoreCount())
                        .reviewCommCount(trend.getReviewCommCount())
                        .couponUsedCount(trend.getCouponUsedCount())
                        .mateCount(trend.getMateCount())
                        .averageRating(trend.getAverageRating() != null ? trend.getAverageRating() : BigDecimal.ZERO)
                        .totalReviewCount(trend.getReviewStoreCount() + trend.getReviewCommCount())
                        .build();
        }

        private static String toTimeRange(String key) {
                try {
                        int block = Integer.parseInt(key);
                        int start = block * 2;
                        int end = start + 1;
                        return String.format("%02d:00~%02d:59", start, end + 1);
                } catch (NumberFormatException e) {
                        return key; // fallback
                }
        }

        private static String capitalize(String text) {
                return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        }
}
