package org.swyp.dessertbee.statistics.store.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "store_statistics_hourly", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"storeId", "date", "hour"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreStatisticsHourly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;

    private LocalDate date;

    private int hour;

    // 통계 필드
    private int viewCount;

    private int saveCount;

    private int reviewStoreCount;

    private int reviewCommCount;

    private int couponUsedCount;

    private int mateCount;

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
