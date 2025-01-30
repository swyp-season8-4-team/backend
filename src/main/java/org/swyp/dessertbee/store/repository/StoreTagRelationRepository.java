package org.swyp.dessertbee.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.entity.Store;
import org.swyp.dessertbee.store.entity.StoreTagRelation;

import java.util.List;

@Repository
public interface StoreTagRelationRepository extends JpaRepository<StoreTagRelation, Long> {
    List<StoreTagRelation> findByStore(Store store);
}
