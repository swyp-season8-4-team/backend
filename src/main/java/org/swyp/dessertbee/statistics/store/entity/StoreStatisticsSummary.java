package org.swyp.dessertbee.statistics.store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 기간 단위(주간/월간/사용자 지정)로 집계된 요약 통계 저장
 * (빠른 조회용)
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "store_statistics_summary", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"store_id", "period_type", "start_date", "end_date"})
})
public class StoreStatisticsSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String periodType; // WEEK, MONTH, CUSTOM

    private Integer totalViews;
    private Integer totalSaves;
    private Integer totalStoreReviewCount;
    private Integer totalCommunityReviewCount;
    private Integer totalDessertMateCount;
    private Integer totalCouponUseCount;

    private BigDecimal averageRating;

    private LocalDateTime createdAt;
}

