package org.swyp.dessertbee.statistics.store.repostiory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.statistics.store.entity.StoreStatisticsSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StoreStatisticsSummaryRepository extends JpaRepository<StoreStatisticsSummary, Long> {
    List<StoreStatisticsSummary> findByStoreIdAndPeriodType(Long storeId, String periodType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    INSERT INTO store_statistics_summary (
        store_id, period_type, start_date, end_date,
        total_views, total_saves, total_store_review_count,
        total_community_review_count, total_dessert_mate_count,
        total_coupon_use_count, average_rating, created_at
    ) VALUES (
        :storeId, :periodType, :startDate, :endDate,
        :views, :saves, :storeReviewCount,
        :communityReviewCount, :mateCount,
        :couponCount, :averageRating
    )
    ON DUPLICATE KEY UPDATE
        total_views = VALUES(total_views),
        total_saves = VALUES(total_saves),
        total_store_review_count = VALUES(total_store_review_count),
        total_community_review_count = VALUES(total_community_review_count),
        total_dessert_mate_count = VALUES(total_dessert_mate_count),
        total_coupon_use_count = VALUES(total_coupon_use_count),
        average_rating = VALUES(average_rating),
        created_at = NOW()
    """, nativeQuery = true)
    void upsertSummary(
            @Param("storeId") Long storeId,
            @Param("periodType") String periodType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("views") int views,
            @Param("saves") int saves,
            @Param("storeReviewCount") int storeReviewCount,
            @Param("communityReviewCount") int communityReviewCount,
            @Param("mateCount") int mateCount,
            @Param("couponCount") int couponCount,
            @Param("averageRating") BigDecimal averageRating
    );

    /**
     * 예전 데이터를 재집계하거나 초기화하기 위함
     */
    void deleteByStoreIdAndPeriodTypeAndStartDateAndEndDate(
            Long storeId,
            String periodType,
            LocalDate startDate,
            LocalDate endDate
    );
}