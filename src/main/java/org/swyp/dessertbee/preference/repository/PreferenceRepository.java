package org.swyp.dessertbee.preference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.preference.entity.PreferenceEntity;

import java.util.Optional;

public interface PreferenceRepository extends JpaRepository<PreferenceEntity, Long> {
    Optional<PreferenceEntity> findByPreferenceName(String preferenceName);
}