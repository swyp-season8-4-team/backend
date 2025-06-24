package org.swyp.dessertbee.store.tag.service;

import org.swyp.dessertbee.store.store.entity.Store;
import org.swyp.dessertbee.store.tag.dto.StoreTagResponse;

import java.util.List;
import java.util.Map;

public interface StoreTagService {
    /** 태그 저장 (1~3개 선택) */
    void saveStoreTags(Store store, List<Long> tagIds);
    /** 태그 조회 */
    List<StoreTagResponse> getTagResponses(Long storeId);
    /** 태그 이름 조회 */
    List<String> getTagNames(Long storeId);

    /**
     * 여러 가게의 태그명 배치 조회
     */
    Map<Long, List<String>> getTagNamesBatch(List<Long> storeIds);

    /**
     * 여러 가게의 태그 응답 배치 조회 (StoreTagResponse용)
     */
    Map<Long, List<StoreTagResponse>> getTagResponsesBatch(List<Long> storeIds);

    /**
     * Fetch Join 사용
     */
    Map<Long, List<String>> getTagNamesBatchWithFetchJoin(List<Long> storeIds);
}
