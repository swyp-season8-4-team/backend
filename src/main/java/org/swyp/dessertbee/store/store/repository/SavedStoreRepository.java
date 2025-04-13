package org.swyp.dessertbee.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.store.dto.response.StoreListLocationResponse;
import org.swyp.dessertbee.store.store.entity.SavedStore;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.entity.UserStoreList;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedStoreRepository extends JpaRepository<SavedStore, Long> {
    List<SavedStore> findByUserStoreList(UserStoreList userStoreList);
    Optional<SavedStore> findByUserStoreListAndStore(UserStoreList userStoreList, Store store);

    @Query("""
        SELECT new org.swyp.dessertbee.store.store.dto.response.StoreListLocationResponse(
            usl.id, usl.iconColorId, s.storeId, s.name, s.latitude, s.longitude
        )
        FROM SavedStore ss
        JOIN ss.userStoreList usl
        JOIN ss.store s
        WHERE usl.id = :listId
    """)
    List<StoreListLocationResponse> findStoresByListId(Long listId);

    /** 여러 저장 리스트에 포함된 모든 가게 조회 */
    List<SavedStore> findByUserStoreListIn(List<UserStoreList> userStoreLists);

    /** 특정 저장 리스트에 저장된 가게 개수 반환 */
    int countByUserStoreList(UserStoreList userStoreList);

    /** 특정 리스트에 저장된 가게 삭제 */
    @Transactional
    void deleteByUserStoreList(UserStoreList userStoreList);

    /** 특정 가게를 저장한 사용자가 있다면 해당 SavedStore 엔티티 반환 */
    @Query("SELECT s FROM SavedStore s " +
            "JOIN s.userStoreList usl " +
            "JOIN usl.user u " +
            "WHERE s.store = :store AND u.id = :userId")
    Optional<SavedStore> findFirstByStoreAndUserId(@Param("store") Store store, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM SavedStore ss WHERE ss.store.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
}