package org.swyp.dessertbee.store.tag.service;

import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.tag.dto.StoreTagResponse;

import java.util.List;

public interface StoreTagService {
    /** 태그 저장 (1~3개 선택) */
    void saveStoreTags(Store store, List<Long> tagIds);
    /** 태그 조회 */
    List<StoreTagResponse> getTagResponses(Long storeId);
    /** 태그 이름 조회 */
    List<String> getTagNames(Long storeId);
}
