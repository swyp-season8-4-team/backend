package org.swyp.dessertbee.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.role.entity.RoleEntity;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
}
