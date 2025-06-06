package org.swyp.dessertbee.store.store.service;

import org.swyp.dessertbee.search.dto.StoreSearchResponse;
import org.swyp.dessertbee.store.store.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface StoreService {
    /**
     * 업주가 등록한 가게 (id, uuid, name) 리스트 조회
     */
    List<StoreShortInfoResponse> getStoresByOwnerUuid(UUID ownerUuid);

    /** 반경 내 가게 조회 */
    List<StoreMapResponse> getStoresByLocation(Double lat, Double lng, Double radius);

    /** 반경 내 특정 취향 태그를 가지는 가게 조회 */
    List<StoreMapResponse> getStoresByLocationAndTags(Double lat, Double lng, Double radius, List<Long> preferenceTagIds);

    /** 반경 내 가게 조회 및 검색 */
    List<StoreMapResponse> getStoresByLocationAndKeyword(Double lat, Double lng, Double radius, String searchKeyword);

    /** 반경 내 사용자 취향 태그에 해당하는 가게 조회 */
    List<StoreMapResponse> getStoresByMyPreferences(Double lat, Double lng, Double radius);

    /** 검색결과와 일치하는 전체 가게 조회 */
    List<StoreSearchResponse> searchStores(String keyword);

    /** 가게 간략 정보 조회 */
    StoreSummaryResponse getStoreSummary(UUID storeUuid);

    /** 가게 상세 정보 조회 */
    StoreDetailResponse getStoreDetails(UUID storeUuid);

    /** 가게의 평균 평점 업데이트 (리뷰 등록,수정,삭제 시 호출) */
    void updateAverageRating(Long storeId);
}
