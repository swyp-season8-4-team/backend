package org.swyp.dessertbee.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.entity.RoleType;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleType name);
    boolean existsByName(RoleType name);
}
