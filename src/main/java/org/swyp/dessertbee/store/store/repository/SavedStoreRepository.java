package org.swyp.dessertbee.store.store.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
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

    /** 특정 저장 리스트에 저장된 가게 개수 반환 */
    int countByUserStoreList(UserStoreList userStoreList);

    /** 특정 리스트에 저장된 가게 삭제 */
    @Transactional
    void deleteByUserStoreList(UserStoreList userStoreList);
}