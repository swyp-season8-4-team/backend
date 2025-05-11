package org.swyp.dessertbee.statistics.store.repostiory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.statistics.store.entity.StoreStatistics;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreStatisticsRepository extends JpaRepository<StoreStatistics, Long> {

    /** 삭제되지 않은 전체 가게 id 조회 */
    @Query("SELECT DISTINCT s.storeId FROM StoreStatistics s WHERE s.deletedAt IS NULL")
    List<Long> findAllStoreIds();

    /** 삭제되지 않은 특정 가게의 전체 통계 조회 */
    List<StoreStatistics> findAllByStoreIdAndDeletedAtIsNull(Long storeId);

    /** 삭제되지 않은 특정 가게의 특정 기간 통계 조회 */
    List<StoreStatistics> findByStoreIdAndDeletedAtIsNullAndStatDateBetween(
            Long storeId, LocalDate start, LocalDate end);

    /** 삭제되지 않은 특정 가게의 특정 기간 통계 조회 (오름차순 정렬) */
    List<StoreStatistics> findByStoreIdAndDeletedAtIsNullAndStatDateBetweenOrderByStatDateAsc(
            Long storeId, LocalDate start, LocalDate end
    );

    /** 한줄리뷰 수 증가 */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE StoreStatistics s SET s.storeReviewCount = s.storeReviewCount + 1 WHERE s.storeId = :storeId")
    void increaseStoreReviewCount(@Param("storeId") Long storeId);

    /** 한줄리뷰 수 감소 (0보다 작아지지 않도록) */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE StoreStatistics s SET s.storeReviewCount = CASE WHEN s.storeReviewCount > 0 THEN s.storeReviewCount - 1 ELSE 0 END WHERE s.storeId = :storeId")
    void decreaseStoreReviewCount(@Param("storeId") Long storeId);

    /** 커뮤니티 리뷰 수 증가 */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE StoreStatistics s SET s.communityReviewCount = s.communityReviewCount + 1 WHERE s.storeId = :storeId")
    void increaseCommunityReviewCount(@Param("storeId") Long storeId);

    /** 커뮤니티 리뷰 수 감소 (0보다 작아지지 않도록) */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE StoreStatistics s SET s.communityReviewCount = CASE WHEN s.communityReviewCount > 0 THEN s.communityReviewCount - 1 ELSE 0 END WHERE s.storeId = :storeId")
    void decreaseCommunityReviewCount(@Param("storeId") Long storeId);
}
