package org.swyp.dessertbee.store.store.service;

import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.store.dto.request.StoreUpdateRequest;
import org.swyp.dessertbee.store.store.dto.response.StoreDetailResponse;
import org.swyp.dessertbee.store.store.dto.response.StoreInfoResponse;
import org.swyp.dessertbee.store.store.dto.response.StoreMapResponse;
import org.swyp.dessertbee.store.store.dto.response.StoreSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface StoreService {
    /** 가게 등록 (이벤트, 쿠폰, 메뉴 + 이미지 포함) */
    void createStore(StoreCreateRequest request,
                                    List<MultipartFile> storeImageFiles,
                                    List<MultipartFile> ownerPickImageFiles,
                                    List<MultipartFile> menuImageFiles);

    /** 반경 내 가게 조회 */
    List<StoreMapResponse> getStoresByLocation(Double lat, Double lng, Double radius);

    /** 반경 내 특정 취향 태그를 가지는 가게 조회 */
    List<StoreMapResponse> getStoresByLocationAndTags(Double lat, Double lng, Double radius, List<Long> preferenceTagIds);

    /** 반경 내 가게 조회 및 검색 */
    List<StoreMapResponse> getStoresByLocationAndKeyword(Double lat, Double lng, Double radius, String searchKeyword);

    /** 반경 내 사용자 취향 태그에 해당하는 가게 조회 */
    List<StoreMapResponse> getStoresByMyPreferences(Double lat, Double lng, Double radius);

    /** 가게 간략 정보 조회 */
    StoreSummaryResponse getStoreSummary(UUID storeUuid);

    /** 가게 상세 정보 조회 */
    StoreDetailResponse getStoreDetails(UUID storeUuid);

    /**
     * 가게 정보 조회
     */
    StoreInfoResponse getStoreInfo(UUID storeUuid);

    /** 가게의 평균 평점 업데이트 (리뷰 등록,수정,삭제 시 호출) */
    void updateAverageRating(Long storeId);

    /** 가게 수정 */
    StoreInfoResponse updateStore(UUID storeUuid,
                                    StoreUpdateRequest request,
                                    List<MultipartFile> storeImageFiles,
                                    List<MultipartFile> ownerPickImageFiles);

    /** 가게 삭제 */
    void deleteStore(UUID storeUuid);
}
