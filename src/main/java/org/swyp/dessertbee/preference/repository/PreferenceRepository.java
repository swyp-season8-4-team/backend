package org.swyp.dessertbee.preference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.dessertbee.preference.entity.PreferenceEntity;

import java.util.List;
import java.util.Optional;

public interface PreferenceRepository extends JpaRepository<PreferenceEntity, Long> {
    Optional<PreferenceEntity> findByPreferenceName(String preferenceName);

    @Query("SELECT p.preferenceName FROM PreferenceEntity p WHERE p.id IN :preferenceTagIds")
    List<String> findPreferenceNamesByIds(@Param("preferenceTagIds") List<Long> preferenceTagIds);

}