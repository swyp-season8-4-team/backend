package org.swyp.dessertbee.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.store.entity.Store;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE Store s SET s.averageRating = :newAverageRating WHERE s.storeId = :storeId AND s.deletedAt IS NULL")
    void updateAverageRating(@Param("storeId") Long storeId, @Param("newAverageRating") BigDecimal newAverageRating);

    @Query(value = """
        SELECT * FROM store
        WHERE ST_Distance_Sphere(point(:lng, :lat), point(longitude, latitude)) <= :radius
        AND deleted_at IS NULL
    """, nativeQuery = true)
    List<Store> findStoresByLocation(@Param("lat") Double lat, @Param("lng") Double lng, @Param("radius") Double radius);

    /**
     * 반경 내 특정 취향 태그를 가지는 가게 조회
     */
    @Query(value = """
    SELECT DISTINCT s.*
    FROM store s
    JOIN store_top_tag stt ON s.store_id = stt.store_id
    WHERE stt.tag_id IN (:preferenceTagIds)
      AND stt.tag_rank <= 3
      AND ST_Distance_Sphere(point(:lng, :lat), point(s.longitude, s.latitude)) <= :radius
      AND s.deleted_at IS NULL
""", nativeQuery = true)
    List<Store> findStoresByLocationAndTags(@Param("lat") Double lat,
                                           @Param("lng") Double lng,
                                           @Param("radius") Double radius, @Param("preferenceTagIds") List<Long> preferenceTagIds);

    /**
     * 반경 내 검색어에 맞는 가게 조회 메서드
     */
    @Query(value = "SELECT DISTINCT s.* " +
            "FROM store s " +
            "LEFT JOIN store_tag_relation str ON s.store_id = str.store_id " +
            "LEFT JOIN store_tag st ON str.tag_id = st.id " +
            "LEFT JOIN menu m ON s.store_id = m.store_id " +
            "WHERE (MATCH(s.name, s.address) AGAINST(:searchKeyword IN NATURAL LANGUAGE MODE) " +
            "   OR MATCH(st.name) AGAINST(:searchKeyword IN NATURAL LANGUAGE MODE) " +
            "   OR MATCH(m.name) AGAINST(:searchKeyword IN NATURAL LANGUAGE MODE)) " +
            "AND ST_Distance_Sphere(point(:lng, :lat), point(s.longitude, s.latitude)) <= :radius " +
            "AND s.deleted_at IS NULL",
            nativeQuery = true)
    List<Store> findStoresByLocationAndKeyword(@Param("lat") Double lat,
                                               @Param("lng") Double lng,
                                               @Param("radius") Double radius,
                                               @Param("searchKeyword") String searchKeyword);

    /**
     * 검색어에 맞는 가게 조회 메서드
     */
    @Query(value = """
        SELECT DISTINCT s.*
        FROM store s
        LEFT JOIN store_tag_relation str ON s.store_id = str.store_id
        LEFT JOIN store_tag st ON str.tag_id = st.id
        LEFT JOIN menu m ON s.store_id = m.store_id
        WHERE (
            MATCH(s.name, s.address) AGAINST(:searchKeyword IN BOOLEAN MODE)
            OR MATCH(st.name) AGAINST(:searchKeyword IN BOOLEAN MODE)
            OR MATCH(m.name) AGAINST(:searchKeyword IN BOOLEAN MODE)
        )
        AND s.deleted_at IS NULL
    """, nativeQuery = true)
    List<Store> findStoresByKeyword(@Param("searchKeyword") String searchKeyword);

    Optional<Store> findByStoreIdAndDeletedAtIsNull(Long storeId);

    @Query("SELECT s.storeId FROM Store s WHERE s.storeUuid = :storeUuid")
    Long findStoreIdByStoreUuid(@Param("storeUuid") UUID storeUuid);

    @Query("SELECT s.storeId FROM Store s WHERE s.name = :name")
    Long findStoreIdByName(@Param("name") String name);


    Store findByName(String placeName);

   Optional<Store> findByStoreUuid(UUID storeUuid);

    List<Store> findAllByOwnerUuidAndDeletedAtIsNull(UUID ownerUuid);

    @Query("SELECT s.averageRating FROM Store s WHERE s.storeId = :storeId")
    BigDecimal findAverageRatingByStoreId(@Param("storeId") Long storeId);
}
