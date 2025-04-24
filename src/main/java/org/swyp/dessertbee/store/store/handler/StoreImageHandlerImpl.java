package org.swyp.dessertbee.store.store.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.store.entity.Store;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreImageHandlerImpl implements StoreImageHandler {

    private final ImageService imageService;

    /**
     * 가게 대표 이미지 처리 메서드
     */
    @Override
    public void updateStoreImages(Store store, List<MultipartFile> storeImageFiles, List<Long> deleteImageIds) {
        if (storeImageFiles != null && !storeImageFiles.isEmpty()) {
            String folder = "store/" + store.getStoreId();
            if (deleteImageIds != null) {
                // 업데이트 시 호출
                imageService.updatePartialImages(deleteImageIds, storeImageFiles, ImageType.STORE, store.getStoreId(), folder);
            } else {
                // 생성 시 호출
                imageService.uploadAndSaveImages(storeImageFiles, ImageType.STORE, store.getStoreId(), folder);
            }
        }
    }

    /**
     * 사장님 픽 이미지 처리 메서드
     */
    @Override
    public void updateOwnerPickImages(Store store, List<MultipartFile> ownerPickImageFiles, List<Long> deleteImageIds) {
        if (ownerPickImageFiles != null && !ownerPickImageFiles.isEmpty()) {
            String folder = "ownerpick/" + store.getStoreId();
            if (deleteImageIds != null) {
                // 업데이트 시 호출
                imageService.updatePartialImages(deleteImageIds, ownerPickImageFiles, ImageType.OWNERPICK, store.getStoreId(), folder);
            } else {
                // 생성 시 호출
                imageService.uploadAndSaveImages(ownerPickImageFiles, ImageType.OWNERPICK, store.getStoreId(), folder);
            }
        }
    }

    /**
     * 가게 대표 이미지 조회 메서드
     */
    @Override
    public List<String> getStoreImages(Long storeId) {
        return imageService.getImagesByTypeAndId(ImageType.STORE, storeId);
    }

    /**
     * 사장님 픽 이미지 조회 메서드
     */
    @Override
    public List<String> getOwnerPickImages(Long storeId) {
        return imageService.getImagesByTypeAndId(ImageType.OWNERPICK, storeId);
    }
}
