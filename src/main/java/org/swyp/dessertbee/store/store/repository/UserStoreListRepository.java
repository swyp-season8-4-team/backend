package org.swyp.dessertbee.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.store.entity.UserStoreList;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;

@Repository
public interface UserStoreListRepository extends JpaRepository<UserStoreList, Long> {
    List<UserStoreList> findByUser(UserEntity user);

    /** 특정 유저가 가진 저장 리스트 개수 반환 */
    long countByUser(UserEntity user);
}