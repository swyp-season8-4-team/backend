package org.swyp.dessertbee.store.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.store.entity.Store;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class StoreMapResponse {
    private Long storeId;
    private UUID storeUuid;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;

    public static StoreMapResponse fromEntity(Store store) {
        return StoreMapResponse.builder()
                .storeId(store.getStoreId())
                .storeUuid(store.getStoreUuid())
                .name(store.getName())
                .address(store.getAddress())
                .latitude(store.getLatitude().doubleValue())
                .longitude(store.getLongitude().doubleValue())
                .build();
    }
}
