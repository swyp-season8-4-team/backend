package org.swyp.dessertbee.store.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.event.entity.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStoreIdAndDeletedAtIsNull(Long storeId);
    Optional<Event> findByIdAndStoreIdAndDeletedAtIsNull(Long eventId, Long storeId);
    boolean existsByStoreIdAndTitleAndStartDateAndDeletedAtIsNull(Long storeId, String title, LocalDate startDate);
    List<Event> findByStoreIdAndDeletedAtIsNullOrderByStartDateAsc(Long storeId);
}