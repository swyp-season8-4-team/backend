package org.swyp.dessertbee.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.entity.Event;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStoreId(Long storeId);
    Optional<Event> findByIdAndStoreId(Long eventId, Long storeId);
}