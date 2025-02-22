package org.swyp.dessertbee.store.store.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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

    // 반경 내 특정 취향 태그(저장된 Top3 취향 태그 중 하나)를 가지는 가게 조회
    @Query(value = """
        SELECT s.*
        FROM store s
        JOIN (
            SELECT ss.store_id, sp.preference,
                   ROW_NUMBER() OVER (PARTITION BY ss.store_id ORDER BY COUNT(*) DESC) AS rn
            FROM saved_store ss
            JOIN saved_store_preferences sp ON ss.id = sp.saved_store_id
            GROUP BY ss.store_id, sp.preference
        ) AS top_pref ON s.store_id = top_pref.store_id
        WHERE top_pref.rn <= 3
          AND top_pref.preference = :preferenceName
          AND ST_Distance_Sphere(point(:lng, :lat), point(s.longitude, s.latitude)) <= :radius
          AND s.deleted_at IS NULL
    """, nativeQuery = true)
    List<Store> findStoresByLocationAndTag(@Param("lat") Double lat,
                                           @Param("lng") Double lng,
                                           @Param("radius") Double radius,
                                           @Param("preferenceName") String preferenceName);

    // 반경 내 검색어에 맞는 가게 조회 메서드
    @Query(value = "SELECT DISTINCT s.* " +
            "FROM store s " +
            "LEFT JOIN store_tag_relation str ON s.store_id = str.store_id " +
            "LEFT JOIN store_tag st ON str.tag_id = st.id " +
            "LEFT JOIN menu m ON s.store_id = m.store_id " +
            "WHERE (s.name LIKE CONCAT('%', :searchKeyword, '%') " +
            "   OR st.name LIKE CONCAT('%', :searchKeyword, '%') " +
            "   OR s.address LIKE CONCAT('%', :searchKeyword, '%') " +
            "   OR m.name LIKE CONCAT('%', :searchKeyword, '%')) " +
            "AND ST_Distance_Sphere(point(:lng, :lat), point(s.longitude, s.latitude)) <= :radius " +
            "AND s.deleted_at IS NULL",
            nativeQuery = true)
    List<Store> findStoresByLocationAndKeyword(@Param("lat") Double lat,
                                               @Param("lng") Double lng,
                                               @Param("radius") Double radius,
                                               @Param("searchKeyword") String searchKeyword);

    // 반경 내 사용자의 취향 태그(Top3 중 하나가 해당하는)를 가지는 가게 조회 메서드
    @Query(value = """
        SELECT s.*
        FROM store s
        JOIN (
            SELECT ss.store_id, sp.preference,
                   ROW_NUMBER() OVER (PARTITION BY ss.store_id ORDER BY COUNT(*) DESC) AS rn
            FROM saved_store ss
            JOIN saved_store_preferences sp ON ss.id = sp.saved_store_id
            GROUP BY ss.store_id, sp.preference
        ) AS top_pref ON s.store_id = top_pref.store_id
        WHERE top_pref.rn <= 3
          AND top_pref.preference IN (:preferenceNames)
          AND ST_Distance_Sphere(point(:lng, :lat), point(s.longitude, s.latitude)) <= :radius
          AND s.deleted_at IS NULL
    """, nativeQuery = true)
    List<Store> findStoresByUserPreferences(@Param("lng") Double lng,
                                            @Param("lat") Double lat,
                                            @Param("radius") Double radius,
                                            @Param("preferenceNames") List<String> preferenceNames);

    Optional<Store> findByStoreIdAndDeletedAtIsNull(Long storeId);

    @Query("SELECT s.storeId FROM Store s WHERE s.storeUuid = :storeUuid")
    Long findStoreIdByStoreUuid(@Param("storeUuid") UUID storeUuid);

    @Query("SELECT s.storeId FROM Store s WHERE s.name = :name")
    Long findStoreIdByName(@Param("name") String name);
}
