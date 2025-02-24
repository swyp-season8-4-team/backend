package org.swyp.dessertbee.mate.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.mate.entity.SavedMate;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedMateRepository extends JpaRepository<SavedMate, Long> {

   SavedMate findByMate_MateIdAndUserId(Long mateId, Long userId);


    void deleteByMate_MateId(Long mateId);

    @Query("SELECT s FROM SavedMate s WHERE s.userId = :userId ORDER BY s.mate.mateId")
    Page<SavedMate> findByUserId(Pageable pageable, Long userId);
}
