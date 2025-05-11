package org.swyp.dessertbee.statistics.store.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.dessertbee.statistics.store.entity.enums.SaveAction;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class StoreSaveActionEvent {
    private final Long storeId;
    private final UUID userUuid;
    private final SaveAction action;
}
