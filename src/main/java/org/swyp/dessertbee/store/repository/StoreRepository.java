package org.swyp.dessertbee.store.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.entity.Store;
import org.swyp.dessertbee.store.entity.StoreStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByStatus(StoreStatus status);

    @Transactional
    @Modifying
    @Query("UPDATE Store s SET s.averageRating = :newAverageRating WHERE s.id = :storeId")
    void updateAverageRating(@Param("storeId") Long storeId, @Param("newAverageRating") BigDecimal newAverageRating);

    @Query(value = """
        SELECT * FROM store 
        WHERE ST_Distance_Sphere(point(:lng, :lat), point(longitude, latitude)) <= :radius
        """, nativeQuery = true)
    List<Store> findStoresByLocation(@Param("lat") Double lat, @Param("lng") Double lng, @Param("radius") Double radius);
}