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

    /**
     * 여러 가게의 태그명을 한 번에 조회
     */
    @Query("SELECT str.store.storeId, t.name " +
            "FROM StoreTagRelation str " +
            "JOIN str.tag t " +
            "WHERE str.store.storeId IN :storeIds")
    List<Object[]> findTagNamesByStoreIds(@Param("storeIds") List<Long> storeIds);

    /**
     * 여러 가게의 태그 정보를 한 번에 조회
     */
    @Query("SELECT str.store.storeId, t.id, t.name, tc.id, tc.name " +
            "FROM StoreTagRelation str " +
            "JOIN str.tag t " +
            "JOIN t.category tc " +
            "WHERE str.store.storeId IN :storeIds")
    List<Object[]> findTagResponsesByStoreIds(@Param("storeIds") List<Long> storeIds);

    /**
     * Fetch Join을 사용한 배치 조회
     */
    @Query("SELECT str FROM StoreTagRelation str " +
            "JOIN FETCH str.tag t " +
            "JOIN FETCH t.category tc " +
            "WHERE str.store.storeId IN :storeIds")
    List<StoreTagRelation> findByStoreIdInWithTagAndCategory(@Param("storeIds") List<Long> storeIds);

    @Transactional
    @Modifying
    void deleteByStore(Store store);

    @Modifying
    @Query("DELETE FROM StoreTagRelation r WHERE r.store.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);

}
