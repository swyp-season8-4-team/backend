package org.swyp.dessertbee.statistics.store.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "기간별 통계 응답 DTO")
public record StoreStatisticsPeriodResponse(

        @Schema(description = "기간 내 총 가게 조회 수", example = "870")
        int totalViews,

        @Schema(description = "기간 내 총 가게 저장 수", example = "210")
        int totalSaves,

        @Schema(description = "기간 내 한줄 리뷰 수", example = "32")
        int totalStoreReviewCount,

        @Schema(description = "기간 내 커뮤니티 리뷰 수", example = "14")
        int totalCommunityReviewCount,

        @Schema(description = "기간 내 전체 리뷰 수", example = "46")
        int totalReviewCount,

        @Schema(description = "기간 내 쿠폰 사용 수", example = "56")
        int totalCouponUsedCount,

        @Schema(description = "기간 내 디저트메이트 모집글 수", example = "8")
        int totalMateCount,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @Schema(description = "평균 평점 (소수점 둘째 자리, 리뷰 존재 시)", example = "4.25", type = "number", format = "double")
        BigDecimal averageRating
) {}