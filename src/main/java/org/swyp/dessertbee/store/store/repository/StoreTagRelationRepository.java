package org.swyp.dessertbee.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.entity.StoreTagRelation;

import java.util.List;

@Repository
public interface StoreTagRelationRepository extends JpaRepository<StoreTagRelation, Long> {

    List<StoreTagRelation> findByStore(Store store);

    // 가게 ID를 기반으로 태그 목록 조회
    @Query("SELECT t.name FROM StoreTagRelation str JOIN str.tag t WHERE str.store.id = :storeId")
    List<String> findTagNamesByStoreId(@Param("storeId") Long storeId);
}
