package org.swyp.dessertbee.migration.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.entity.StoreLink;
import org.swyp.dessertbee.store.store.repository.StoreLinkRepository;
import org.swyp.dessertbee.store.store.repository.StoreRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreLinkMigrationService {

    private final StoreRepository storeRepository;
    private final StoreLinkRepository storeLinkRepository;

    @Transactional
    public void migrateStoreLinks() {
        List<Store> stores = storeRepository.findAll();

        List<StoreLink> storeLinks = stores.stream()
                .filter(store -> store.getStoreLink() != null && !store.getStoreLink().isBlank())
                .filter(store -> storeLinkRepository.countByStoreId(store.getStoreId()) == 0) // 중복 방지
                .map(store -> StoreLink.builder()
                        .storeId(store.getStoreId())
                        .url(store.getStoreLink())
                        .isPrimary(true)
                        .build())
                .collect(Collectors.toList());

        storeLinkRepository.saveAll(storeLinks);
        System.out.println("✅ 마이그레이션 완료 - 총 " + storeLinks.size() + "건");
    }
}
