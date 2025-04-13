package org.swyp.dessertbee.store.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.StoreRepository;
import org.swyp.dessertbee.store.store.repository.StoreTagRelationRepository;
import org.swyp.dessertbee.store.store.exception.StoreExceptions.*;

import java.util.List;

@RequiredArgsConstructor
@Service
public class StoreTagServiceImpl implements StoreTagService {
    private final StoreRepository storeRepository;
    private final StoreTagRelationRepository storeTagRelationRepository;

    @Override
    public List<String> getTags(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException());

        return storeTagRelationRepository.findTagNamesByStoreId(storeId);
    }
}