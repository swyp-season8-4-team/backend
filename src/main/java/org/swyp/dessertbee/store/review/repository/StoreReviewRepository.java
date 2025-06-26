package org.swyp.dessertbee.store.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.review.entity.StoreReview;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public interface StoreReviewRepository extends JpaRepository<StoreReview, Long> {

    /** 특정 가게의 평균 평점 조회 (리뷰가 없으면 0.0 반환) */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM StoreReview r WHERE r.storeId = :storeId AND r.deletedAt IS NULL")
    BigDecimal findAverageRatingByStoreId(@Param("storeId") Long storeId);

    /** 특정 가게에 존재하는 리뷰 목록 조회 **/
    @Query("SELECT sr FROM StoreReview sr WHERE sr.storeId = :storeId AND sr.deletedAt IS NULL ORDER BY sr.createdAt DESC")
    List<StoreReview> findByStoreIdAndDeletedAtIsNull(@Param("storeId") Long storeId);

    Optional<StoreReview> findByReviewIdAndDeletedAtIsNull(Long reviewId);

    @Query("SELECT r.reviewId FROM StoreReview r WHERE r.reviewUuid = :reviewUuid")
    Long findReviewIdByReviewUuid(@Param("reviewUuid") UUID reviewUuid);

    @Query("SELECT COUNT(r) FROM StoreReview r WHERE r.storeId = :storeId AND r.deletedAt IS NULL")
    int countByStoreIdAndDeletedAtIsNull(@Param("storeId") Long storeId);

    /**
     * 특정 유저가 특정 가게에 "오늘" 날짜로 작성한 리뷰가 있는지 체크
     */
    @Query("""
        SELECT COUNT(r) FROM StoreReview r
        WHERE r.storeId = :storeId
          AND r.userUuid = :userUuid
          AND r.deletedAt IS NULL
          AND FUNCTION('DATE', r.createdAt) = CURRENT_DATE
    """)
    int countTodayReviewsByUserAndStore(@Param("userUuid") UUID userUuid, @Param("storeId") Long storeId);

    @Query("""
        SELECT sr FROM StoreReview sr
        WHERE sr.userUuid = :userUuid AND sr.deletedAt IS NULL
        ORDER BY sr.createdAt DESC
    """)
    List<StoreReview> findByUserUuidOrderByCreatedAtDesc(@Param("userUuid") UUID userUuid);


    Optional<StoreReview> findByReviewUuid(UUID reviewUuid);

    /**
     * 여러 가게의 리뷰 개수 배치 조회
     */
    @Query("SELECT sr.storeId, COUNT(sr) FROM StoreReview sr " +
            "WHERE sr.storeId IN :storeIds AND sr.deletedAt IS NULL " +
            "GROUP BY sr.storeId")
    List<Object[]> findReviewCountsByStoreIds(@Param("storeIds") List<Long> storeIds);

    /**
     * Map으로 변환한 리뷰 개수 배치 조회
     */
    default Map<Long, Integer> getReviewCountsBatch(List<Long> storeIds) {
        List<Object[]> results = findReviewCountsByStoreIds(storeIds);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> ((Long) result[1]).intValue()
                ));
    }
}
