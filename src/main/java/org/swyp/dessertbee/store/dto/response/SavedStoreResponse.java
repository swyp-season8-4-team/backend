package org.swyp.dessertbee.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.entity.SavedStore;
import org.swyp.dessertbee.store.entity.Store;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class SavedStoreResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private String address;
    private String storeLink;
    private LocalDateTime savedAt;

    public static SavedStoreResponse fromEntity(SavedStore savedStore, Store store) {
        return SavedStoreResponse.builder()
                .id(savedStore.getId())
                .storeId(store.getId())
                .storeName(store.getName())
                .address(store.getAddress())
                .storeLink(store.getStoreLink())
                .savedAt(savedStore.getCreatedAt())
                .build();
    }
}
