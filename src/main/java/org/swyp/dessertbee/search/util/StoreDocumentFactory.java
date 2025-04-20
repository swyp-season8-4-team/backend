package org.swyp.dessertbee.search.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.search.doc.StoreDocument;
import org.swyp.dessertbee.store.menu.repository.MenuRepository;
import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.store.repository.StoreTagRelationRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StoreDocumentFactory {

    private final StoreTagRelationRepository storeTagRelationRepository;
    private final MenuRepository menuRepository;

    public StoreDocument fromStore(Store store) {
        List<String> tagNames = storeTagRelationRepository.findTagNamesByStoreId(store.getStoreId());
        List<String> menuNames = menuRepository.findMenuNamesByStoreId(store.getStoreId());

        return StoreDocument.builder()
                .storeId(store.getStoreId())
                .storeUuid(store.getStoreUuid())
                .storeName(store.getName())
                .address(store.getAddress())
                .tagNames(tagNames)
                .menuNames(menuNames)
                .deleted(store.getDeletedAt() != null)
                .build();
    }
}
