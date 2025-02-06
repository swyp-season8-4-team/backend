package org.swyp.dessertbee.store.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.store.store.dto.response.SavedStoreResponse;
import org.swyp.dessertbee.store.store.entity.SavedStore;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.SavedStoreRepository;
import org.swyp.dessertbee.store.store.repository.StoreRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SavedStoreService {

    private final SavedStoreRepository savedStoreRepository;
    private final StoreRepository storeRepository;

    /** 유저가 저장한 가게 목록 조회 (soft delete 반영) */
    public List<SavedStoreResponse> getSavedStoresByUser(Long userId) {
        List<SavedStore> savedStores = savedStoreRepository.findByUserId(userId);

        return savedStores.stream()
                .map(saved -> {
                    Store store = storeRepository.findByIdAndDeletedAtIsNull(saved.getStoreId())
                            .orElse(null); // 삭제된 가게는 리스트에서 제외

                    if (store == null) {
                        return null; // 삭제된 가게 필터링
                    }
                    return SavedStoreResponse.fromEntity(saved, store);
                })
                .filter(Objects::nonNull) // 삭제된 가게 제거
                .collect(Collectors.toList());
    }
}
