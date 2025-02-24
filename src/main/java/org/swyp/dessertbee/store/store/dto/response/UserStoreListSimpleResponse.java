package org.swyp.dessertbee.store.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserStoreListSimpleResponse {
    private Long listId;
    private String listName;
    private Long iconColorId;
}
