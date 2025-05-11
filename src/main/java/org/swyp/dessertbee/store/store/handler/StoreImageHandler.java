package org.swyp.dessertbee.store.store.handler;

import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.store.dto.response.StoreImageResponse;
import org.swyp.dessertbee.store.store.entity.Store;

import java.util.List;

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
}
