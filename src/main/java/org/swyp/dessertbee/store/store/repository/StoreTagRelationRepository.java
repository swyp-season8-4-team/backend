package org.swyp.dessertbee.store.store.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.entity.StoreTagRelation;

import java.util.List;
import java.util.UUID;

@Repository
public interface StoreTagRelationRepository extends JpaRepository<StoreTagRelation, Long> {

    List<StoreTagRelation> findByStore(Store store);

    // 가게 ID를 기반으로 태그 목록 조회
    @Query("SELECT t.name FROM StoreTagRelation str JOIN str.tag t WHERE str.store.storeUuid = :storeUuid")
    List<String> findTagNamesByStoreId(@Param("storeUuid") UUID storeUuid);

    @Transactional
    @Modifying
    void deleteByStore(Store store);

}
