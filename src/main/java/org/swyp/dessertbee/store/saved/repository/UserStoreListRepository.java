package org.swyp.dessertbee.store.saved.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.saved.entity.UserStoreList;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;

@Repository
public interface UserStoreListRepository extends JpaRepository<UserStoreList, Long> {
    List<UserStoreList> findByUser(UserEntity user);

    /** 특정 유저가 같은 이름을 가진 저장 리스트가 있는지 확인 */
    boolean existsByUserAndListName(UserEntity user, String listName);

    /**
     * 특정 사용자의 모든 스토어 리스트 삭제
     */
    @Modifying
    @Query("DELETE FROM UserStoreList usl WHERE usl.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}