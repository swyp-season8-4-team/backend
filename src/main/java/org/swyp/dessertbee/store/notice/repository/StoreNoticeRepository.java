package org.swyp.dessertbee.store.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.store.notice.entity.StoreNotice;

import java.util.List;
import java.util.Optional;

public interface StoreNoticeRepository extends JpaRepository<StoreNotice, Long> {
    List<StoreNotice> findAllByStoreIdAndDeletedAtIsNull(Long storeId);
    StoreNotice findByNoticeIdAndDeletedAtIsNull(Long noticeId);
    Optional<StoreNotice> findFirstByStoreIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long storeId);
}