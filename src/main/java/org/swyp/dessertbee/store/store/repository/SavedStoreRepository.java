package org.swyp.dessertbee.store.store.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.store.entity.SavedStore;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.entity.UserStoreList;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedStoreRepository extends JpaRepository<SavedStore, Long> {
    List<SavedStore> findByUserStoreList(UserStoreList userStoreList);
    Optional<SavedStore> findByUserStoreListAndStore(UserStoreList userStoreList, Store store);

    /** 여러 저장 리스트에 포함된 모든 가게 조회 */
    List<SavedStore> findByUserStoreListIn(List<UserStoreList> userStoreLists);

    /** 특정 저장 리스트에 저장된 가게 개수 반환 */
    int countByUserStoreList(UserStoreList userStoreList);

    /** 특정 리스트에 저장된 가게 삭제 */
    @Transactional
    void deleteByUserStoreList(UserStoreList userStoreList);

    /** 특정 가게를 저장한 사람들의 취향 태그 Top3 조회 */
    @Query(value = """
    SELECT p.preference_name, COUNT(sp.preference) AS preference_count
    FROM saved_store_preferences sp
    JOIN preference p ON sp.preference = p.id
    WHERE sp.saved_store_id IN (
        SELECT id FROM saved_store WHERE store_id = :storeId
    )
    GROUP BY p.preference_name
    ORDER BY preference_count DESC
    LIMIT 3
""", nativeQuery = true)
    List<Object[]> findTop3PreferencesByStoreId(@Param("storeId") Long storeId);

    /** 특정 가게를 저장한 사용자가 있다면 해당 SavedStore 엔티티 반환 */
    @Query("SELECT s FROM SavedStore s " +
            "JOIN s.userStoreList usl " +
            "JOIN usl.user u " +
            "WHERE s.store = :store AND u.id = :userId")
    Optional<SavedStore> findFirstByStoreAndUserId(@Param("store") Store store, @Param("userId") Long userId);
}