package org.swyp.dessertbee.store.link.service;

import com.nimbusds.jose.util.Pair;
import org.swyp.dessertbee.store.store.dto.request.BaseStoreRequest;
import org.swyp.dessertbee.store.store.entity.Store;

import java.util.List;

public interface StoreLinkService {
    /** 가게 링크 저장 */
    void validateAndSaveStoreLinks(Store store, List<? extends BaseStoreRequest.StoreLinkRequest> linkRequests);
    /** 가게 링크/대표링크 조회 */
    Pair<List<String>, String> getStoreLinksAndPrimary(Long storeId);
}
