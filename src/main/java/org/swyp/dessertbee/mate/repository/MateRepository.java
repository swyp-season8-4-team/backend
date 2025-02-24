package org.swyp.dessertbee.mate.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.mate.entity.Mate;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MateRepository extends JpaRepository<Mate, Long> {

    @Transactional
    Optional<Mate> findByMateUuidAndDeletedAtIsNull(UUID mateUuid);

    @Query("SELECT m FROM Mate m WHERE m.storeId = :storeId AND m.deletedAt IS NULL")
    List<Mate> findByStoreIdAndDeletedAtIsNull(@Param("storeId") Long storeId);

    /**
     * MateUuid로 MateId 조회
     * */
    @Query("SELECT m.mateId FROM Mate m where m.mateUuid = :mateUuid")
    Optional<Long> findMateIdByMateUuid(UUID mateUuid);

    /**
     *
     * */
    @Query("SELECT m FROM Mate m WHERE m.deletedAt IS NULL ORDER BY m.mateId")
    Page<Mate> findAllByDeletedAtIsNull(Pageable pageable);


    @Query("SELECT m FROM Mate m WHERE m.deletedAt IS NULL AND m.mateId IN :mateIds")
    List<Mate> findByMateIdIn(@Param("mateIds") List<Long> mateIds);
}
