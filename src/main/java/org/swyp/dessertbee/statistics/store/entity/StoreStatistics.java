package org.swyp.dessertbee.statistics.store.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "store_statistics")
public class StoreStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statisticsId;

    @Column(name = "store_id")
    private Long storeId;

    @Column(nullable = false)
    private Integer views = 0;

    @Column(nullable = false)
    private Integer saves = 0;

    @Column(name = "store_review_count", nullable = false)
    private Integer storeReviewCount = 0;

    @Column(name = "community_review_count", nullable = false)
    private Integer communityReviewCount = 0;

    @Column(name = "dessert_mate_count", nullable = false)
    private Integer dessertMateCount = 0;

    @Column(name = "coupon_use_count", nullable = false)
    private Integer couponUseCount = 0;

    @Column(name = "average_rating", precision = 2, scale = 1)
    private BigDecimal averageRating;

    private LocalDate createDate; // 가게가 등록된 날짜

    @CreationTimestamp
    private LocalDateTime createdAt; // 가게 통계가 집계된 시간

    private LocalDateTime deletedAt; // 가게, 통계가 삭제(무효화)된 시간

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
