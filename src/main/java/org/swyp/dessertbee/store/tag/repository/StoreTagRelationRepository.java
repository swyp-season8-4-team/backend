package org.swyp.dessertbee.store.tag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.tag.entity.StoreTag;
import org.swyp.dessertbee.store.tag.entity.StoreTagRelation;

import java.util.List;

@Repository
public interface StoreTagRelationRepository extends JpaRepository<StoreTagRelation, Long> {

    List<StoreTagRelation> findByStore(Store store);

    // 가게 ID를 기반으로 태그 목록 조회
    @Query("SELECT t.name FROM StoreTagRelation str JOIN str.tag t WHERE str.store.storeId = :storeId")
    List<String> findTagNamesByStoreId(@Param("storeId") Long storeId);

    @Query("""
    SELECT st
    FROM StoreTagRelation str
    JOIN str.tag st
    WHERE str.store.storeId = :storeId
    """)
    List<StoreTag> findTagsByStoreId(@Param("storeId") Long storeId);

    @Transactional
    @Modifying
    void deleteByStore(Store store);

    @Modifying
    @Query("DELETE FROM StoreTagRelation r WHERE r.store.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);

}
