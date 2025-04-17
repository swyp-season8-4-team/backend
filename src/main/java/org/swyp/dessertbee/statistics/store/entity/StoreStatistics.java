package org.swyp.dessertbee.statistics.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일 단위의 통계 데이터를 저장
 * (원본 로그 기반)
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "store_statistics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"store_id", "stat_date"})
})
public class StoreStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "통계 ID", example = "1")
    private Long statisticsId;

    @Column(name = "store_id", nullable = false)
    @Schema(description = "가게 ID", example = "1001")
    private Long storeId;

    @Column(name = "stat_date", nullable = false)
    @Schema(description = "통계 집계 기준 날짜", example = "2025-04-17")
    private LocalDate statDate;

    @Builder.Default
    @Column(nullable = false)
    @Schema(description = "가게 조회 수", example = "150")
    private Integer views = 0;

    @Builder.Default
    @Column(nullable = false)
    @Schema(description = "가게 저장 수", example = "45")
    private Integer saves = 0;

    @Builder.Default
    @Column(name = "store_review_count", nullable = false)
    @Schema(description = "가게 자체 리뷰 수", example = "12")
    private Integer storeReviewCount = 0;

    @Builder.Default
    @Column(name = "community_review_count", nullable = false)
    @Schema(description = "커뮤니티 연동 리뷰 수", example = "7")
    private Integer communityReviewCount = 0;

    @Builder.Default
    @Column(name = "dessert_mate_count", nullable = false)
    @Schema(description = "디저트 메이트 매칭 수", example = "3")
    private Integer dessertMateCount = 0;

    @Builder.Default
    @Column(name = "coupon_use_count", nullable = false)
    @Schema(description = "쿠폰 사용 횟수", example = "25")
    private Integer couponUseCount = 0;

    @Builder.Default
    @Column(name = "average_rating", precision = 2, scale = 1, nullable = false)
    @Schema(description = "가게 평균 평점", example = "4.3")
    private BigDecimal averageRating = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "통계 레코드 생성 시각", example = "2025-04-17T00:00:00")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    @Schema(description = "소프트 삭제된 시각 (null이면 유효)", example = "2025-04-20T00:00:00", nullable = true)
    private LocalDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}