package org.swyp.dessertbee.store.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class UserStoreListResponse {
    private Long listId;
    private UUID userUuid;
    private String listName;
    private Long iconColorId;
    private int storeCount;
}