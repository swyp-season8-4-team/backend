package org.swyp.dessertbee.statistics.store.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "시간 단위 통계 정보")
public class StoreStatisticsHourly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "고유 ID", example = "1")
    private Long id;

    @Schema(description = "가게 ID", example = "101")
    private Long storeId;

    @Schema(description = "통계 일자", example = "2025-05-24")
    private LocalDate date;

    @Schema(description = "해당 날짜의 시간대 (0~23)", example = "13")
    private int hour;

    // 통계 필드
    @Schema(description = "해당 시간대 가게 조회 수", example = "120")
    private int viewCount;

    @Schema(description = "해당 시간대 가게 저장 수", example = "35")
    private int saveCount;

    @Schema(description = "한줄 리뷰 수", example = "5")
    private int reviewStoreCount;

    @Schema(description = "커뮤니티 리뷰 수", example = "2")
    private int reviewCommCount;

    @Schema(description = "쿠폰 사용 수", example = "9")
    private int couponUsedCount;

    @Schema(description = "디저트 메이트 모집글 수", example = "1")
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
