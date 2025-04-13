package org.swyp.dessertbee.statistics.store.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.dessertbee.statistics.store.entity.enums.DessertMateAction;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MateActionEvent {
    private final Long storeId;
    private final Long mateId;
    private final UUID userUuid;
    private final DessertMateAction action;
}