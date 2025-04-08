package org.swyp.dessertbee.store.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.store.notice.entity.StoreNotice;

import java.util.List;

public interface StoreNoticeRepository extends JpaRepository<StoreNotice, Long> {
    List<StoreNotice> findAllByStoreIdAndDeletedAtIsNull(Long storeId);
}