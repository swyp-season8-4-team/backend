package org.swyp.dessertbee.statistics.store.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class StoreViewEvent {
    private final Long storeId;
    private final UUID userUuid;
}
