package org.swyp.dessertbee.store.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.event.entity.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByEventIdAndStoreIdAndDeletedAtIsNull(Long eventId, Long storeId);
    List<Event> findByStoreIdAndDeletedAtIsNullOrderByStartDateAsc(Long storeId);
    Long findEventIdByEventUuid(UUID eventUuid);
}