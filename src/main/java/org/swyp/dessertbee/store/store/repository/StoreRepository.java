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

    Optional<Store> findByStoreIdAndDeletedAtIsNull(Long storeId);

    @Query("SELECT s.storeId FROM Store s WHERE s.storeUuid = :storeUuid")
    Long findStoreIdByStoreUuid(@Param("storeUuid") UUID storeUuid);
}
