package org.swyp.dessertbee.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.role.entity.UserRoleEntity;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {
    List<UserRoleEntity> findByUserId(Long userId); // 사용자의 모든 역할 가져오기
}
