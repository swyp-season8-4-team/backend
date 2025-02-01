package org.swyp.dessertbee.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.swyp.dessertbee.store.entity.Store;

@Data
@Builder
@AllArgsConstructor
public class StoreMapResponse {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;

    public static StoreMapResponse fromEntity(Store store) {
        return StoreMapResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .latitude(store.getLatitude().doubleValue())
                .longitude(store.getLongitude().doubleValue())
                .build();
    }
}
