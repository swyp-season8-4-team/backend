package org.swyp.dessertbee.store.store.handler;

import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.store.dto.response.StoreImageResponse;
import org.swyp.dessertbee.store.store.entity.Store;

import java.util.List;
import java.util.Map;

public interface StoreImageHandler {
    /** 가게 대표 이미지 처리 */
    void updateStoreImages(Store store, List<MultipartFile> newImages, List<Long> deleteImageIds);
    /** 가게 사장님픽 이미지 처리 */
    void updateOwnerPickImages(Store store, List<MultipartFile> newImages, List<Long> deleteImageIds);
    /**
     * 가게 대표 이미지 조회 메서드
     */
    List<StoreImageResponse> getStoreImages(Long storeId);
    /**
     * 사장님 픽 이미지 조회 메서드
     */
    List<StoreImageResponse> getOwnerPickImages(Long storeId);

    // TODO: 여러 가게의 대표 이미지 배치 조회 (ID + URL)
    Map<Long, List<StoreImageResponse>> getStoreImagesBatch(List<Long> storeIds);

    // TODO: 여러 가게의 업주 선택 이미지 배치 조회 (ID + URL)
    Map<Long, List<StoreImageResponse>> getOwnerPickImagesBatch(List<Long> storeIds);

    /**
     * 가게 이미지 URL만 배치 조회
     */
    Map<Long, List<String>> getStoreImageUrlsBatch(List<Long> storeIds);
}
