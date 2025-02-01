package org.swyp.dessertbee.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.store.dto.response.SavedStoreResponse;
import org.swyp.dessertbee.store.entity.SavedStore;
import org.swyp.dessertbee.store.entity.Store;
import org.swyp.dessertbee.store.repository.SavedStoreRepository;
import org.swyp.dessertbee.store.repository.StoreRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SavedStoreService {

    private final SavedStoreRepository savedStoreRepository;
    private final StoreRepository storeRepository;

    /** 유저가 저장한 가게 목록 조회 */
    public List<SavedStoreResponse> getSavedStoresByUser(Long userId) {
        List<SavedStore> savedStores = savedStoreRepository.findByUserId(userId);

        return savedStores.stream()
                .map(saved -> {
                    Store store = storeRepository.findById(saved.getStoreId())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 가게: " + saved.getStoreId()));

                    return SavedStoreResponse.fromEntity(saved, store);
                })
                .collect(Collectors.toList());
    }
}
