package org.swyp.dessertbee.mate.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.mate.entity.Mate;

import java.util.Optional;

@Repository
public interface MateRepository extends JpaRepository<Mate, Long> {

    @Transactional
    Optional<Mate> findByMateIdAndDeletedAtIsNull(Long mateId);
}
