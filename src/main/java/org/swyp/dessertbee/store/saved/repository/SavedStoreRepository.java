package org.swyp.dessertbee.store.saved.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.saved.dto.StoreListLocationResponse;
import org.swyp.dessertbee.store.saved.entity.SavedStore;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.saved.entity.UserStoreList;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedStoreRepository extends JpaRepository<SavedStore, Long> {
    /**
     * 특정 저장 리스트에 포함된 모든 SavedStore 조회
     */
    List<SavedStore> findByUserStoreList(UserStoreList userStoreList);

    /**
     * 특정 저장 리스트에 특정 가게가 이미 저장되어 있는지 여부 확인
     */
    Optional<SavedStore> findByUserStoreListAndStore(UserStoreList userStoreList, Store store);


    /**
     * 특정 저장 리스트에 속한 가게들의 위치 정보 조회
     */
    @Query("""
        SELECT new org.swyp.dessertbee.store.saved.dto.StoreListLocationResponse(
            usl.id,
            usl.iconColorId,
            s.storeId,
            s.storeUuid,
            s.name,
            s.latitude,
            s.longitude
        )
        FROM SavedStore ss
        JOIN ss.userStoreList usl
        JOIN ss.store s
        WHERE usl.id = :listId
    """)
    List<StoreListLocationResponse> findStoresByListId(Long listId);

    /**
     * 유저가 저장한 모든 가게들의 위치 정보 조회
     */
    @Query("""
        SELECT new org.swyp.dessertbee.store.saved.dto.StoreListLocationResponse(
            usl.id, usl.iconColorId, s.storeId, s.storeUuid, s.name, s.latitude, s.longitude
        )
        FROM SavedStore ss
        JOIN ss.userStoreList usl
        JOIN ss.store s
        WHERE usl.user.id = :userId
    """)
    List<StoreListLocationResponse> findAllLocationByUserId(@Param("userId") Long userId);

    /**
     * 여러 저장 리스트에 포함된 SavedStore 전체 조회
     */
    List<SavedStore> findByUserStoreListIn(List<UserStoreList> userStoreLists);

    /**
     * 특정 저장 리스트에 저장된 가게 개수 반환
     */
    int countByUserStoreList(UserStoreList userStoreList);

    /**
     * 특정 저장 리스트에 저장된 모든 SavedStore 삭제
     */
    @Transactional
    void deleteByUserStoreList(UserStoreList userStoreList);

    /**
     * 특정 가게를 저장한 사용자가 있는지 확인하고 해당 SavedStore 반환
     */
    @Query("""
        SELECT s
        FROM SavedStore s
        JOIN s.userStoreList usl
        JOIN usl.user u
        WHERE s.store = :store
          AND u.id = :userId
    """)
    Optional<SavedStore> findFirstByStoreAndUserId(@Param("store") Store store, @Param("userId") Long userId);

    /**
     * 특정 가게와 연결된 모든 SavedStore 삭제
     */
    @Modifying
    @Query("""
        DELETE FROM SavedStore ss
        WHERE ss.store.storeId = :storeId
    """)
    void deleteByStoreId(@Param("storeId") Long storeId);


    /**
     * 특정 유저가 저장한 모든 SavedStore + Store + UserStoreList까지 fetch join으로 한번에 조회
     * → N+1 문제 방지를 위한 최적화용
     */
    @Query("""
        SELECT ss
        FROM SavedStore ss
        JOIN FETCH ss.store s
        JOIN FETCH ss.userStoreList l
        WHERE l.user.id = :userId
    """)
    List<SavedStore> findAllByUserIdWithStoreAndList(@Param("userId") Long userId);

    /** 특정 가게를 유저가 저장한 SavedStore 모두 조회 */
    @Query("""
        SELECT ss
        FROM SavedStore ss
        JOIN ss.userStoreList usl
        WHERE ss.store = :store
          AND usl.user.id = :userId
    """)
    List<SavedStore> findByStoreAndUserId(@Param("store") Store store, @Param("userId") Long userId);

    /** 특정 리스트-가게 조합으로 SavedStore 삭제 */
    @Transactional
    @Modifying
    @Query("""
        DELETE FROM SavedStore ss
        WHERE ss.userStoreList = :userStoreList
          AND ss.store = :store
    """)
    void deleteByUserStoreListAndStore(@Param("userStoreList") UserStoreList userStoreList, @Param("store") Store store);
}