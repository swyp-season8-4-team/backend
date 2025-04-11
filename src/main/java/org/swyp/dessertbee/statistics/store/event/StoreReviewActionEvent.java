package org.swyp.dessertbee.statistics.store.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.swyp.dessertbee.statistics.store.entity.enums.ReviewAction;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class StoreReviewActionEvent {
    private final Long storeId;
    private final Long reviewId;
    private final UUID userUuid;
    private final ReviewAction action;
}
