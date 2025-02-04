package org.swyp.dessertbee.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.auth.entity.AuthEntity;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<AuthEntity, Integer> {
    Optional<AuthEntity> findByUserAndProvider(UserEntity user, String provider);
}
