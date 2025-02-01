package org.swyp.dessertbee.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.entity.StoreReview;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface StoreReviewRepository extends JpaRepository<StoreReview, Long> {

    /** 특정 가게의 평균 평점 조회 (리뷰가 없으면 0.0 반환) */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM StoreReview r WHERE r.storeId = :storeId")
    BigDecimal findAverageRatingByStoreId(@Param("storeId") Long storeId);

    /** 특정 가게의 리뷰 목록 조회 **/
    List<StoreReview> findByStoreId(Long storeId);
}
