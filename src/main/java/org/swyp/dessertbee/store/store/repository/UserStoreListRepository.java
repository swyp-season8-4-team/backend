package org.swyp.dessertbee.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.store.entity.UserStoreList;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;

@Repository
public interface UserStoreListRepository extends JpaRepository<UserStoreList, Long> {
    List<UserStoreList> findByUser(UserEntity user);

    /** 특정 유저가 같은 이름을 가진 저장 리스트가 있는지 확인 */
    boolean existsByUserAndListName(UserEntity user, String listName);

    /** 특정 유저가 같은 아이콘 색상을 가진 저장 리스트가 있는지 확인 */
    boolean existsByUserAndIconColorId(UserEntity user, Long iconColorId);
}