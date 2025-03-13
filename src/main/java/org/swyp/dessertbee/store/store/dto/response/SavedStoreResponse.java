package org.swyp.dessertbee.store.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class SavedStoreResponse {
    private UUID userUuid;
    private UUID storeUuid;
    private Long listId;
    private String listName;
    private String storeName;
    private String storeAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> imageUrls;
    private List<Long> userPreferences;
}
