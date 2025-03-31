package org.swyp.dessertbee.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.store.entity.StoreLink;

import java.util.List;

@Repository
public interface StoreLinkRepository extends JpaRepository<StoreLink, Long> {
    List<StoreLink> findByStoreId(Long storeId);
    void deleteByStoreId(Long storeId);
    long countByStoreId(Long storeId);
}