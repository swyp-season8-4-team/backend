package org.swyp.dessertbee.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.swyp.dessertbee.role.entity.UserRoleEntity;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {
    List<UserRoleEntity> findByUserId(Long userId); // 사용자의 모든 역할 가져오기

    @Query("SELECT COUNT(DISTINCT ur.user.id) " +
            "FROM UserRoleEntity ur " +
            "WHERE ur.role.id != 3")
    long countUsers();

    @Query("SELECT COUNT(DISTINCT ur.user.id) " +
            "FROM UserRoleEntity ur " +
            "WHERE ur.role.id = 2")
    long countOwners();

}
