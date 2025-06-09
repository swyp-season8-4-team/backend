package org.swyp.dessertbee.statistics.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.swyp.dessertbee.statistics.store.entity.enums.PeriodType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "store_statistics_trend", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"storeId", "date", "periodType", "trendKey"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "선택일 기준 추이 통계 정보 (시간, 요일, 일 단위)")
public class StoreStatisticsTrend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "고유 ID", example = "1")
    private Long id;

    @Schema(description = "가게 ID", example = "101")
    private Long storeId;

    @Schema(description = "기준일", example = "2025-06-09")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "기간 유형", example = "DAILY")
    private PeriodType periodType;

    @Column(length = 10, nullable = false)
    @Schema(description = "추이 구간 (일간-DAILY: 2시간 단위 시간대, 주간-WEEKLY: 요일명, 월간-MONTHLY: N일)", example = "14:00~15:59")
    private String displayKey;

    @Schema(description = "조회 수", example = "120")
    private int viewCount;

    @Schema(description = "저장 수", example = "35")
    private int saveCount;

    @Schema(description = "한줄 리뷰 수", example = "5")
    private int reviewStoreCount;

    @Schema(description = "커뮤니티 리뷰 수", example = "2")
    private int reviewCommCount;

    @Schema(description = "쿠폰 사용 수", example = "9")
    private int couponUsedCount;

    @Schema(description = "디저트 메이트 생성 수", example = "1")
    private int mateCount;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Schema(description = "해당 구간의 평균 평점", example = "4.27")
    private BigDecimal averageRating;

    public void addViewCount(int delta) {
        this.viewCount += delta;
    }

    public void addSaveCount(int delta) {
        this.saveCount += delta;
    }

    public void addReviewStoreCount(int delta) {
        this.reviewStoreCount += delta;
    }

    public void addReviewCommCount(int delta) {
        this.reviewCommCount += delta;
    }

    public void addCouponUsedCount(int delta) {
        this.couponUsedCount += delta;
    }

    public void addMateCount(int delta) {
        this.mateCount += delta;
    }
}