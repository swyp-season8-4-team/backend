package org.swyp.dessertbee.store.store.service;

import java.util.List;

public interface StoreTagService {
    List<String> getTags(Long storeId);
}
