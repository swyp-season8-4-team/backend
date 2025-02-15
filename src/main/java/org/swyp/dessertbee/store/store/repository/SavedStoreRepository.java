package org.swyp.dessertbee.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.store.entity.SavedStore;

import java.util.List;

@Repository
public interface SavedStoreRepository extends JpaRepository<SavedStore, Long> {

    List<SavedStore> findByUserId(Long userId);
}
