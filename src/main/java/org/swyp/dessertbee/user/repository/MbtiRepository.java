package org.swyp.dessertbee.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.user.entity.MbtiEntity;

import java.util.Optional;

public interface MbtiRepository extends JpaRepository<MbtiEntity, Long> {
    Optional<MbtiEntity> findByType(String type);
}