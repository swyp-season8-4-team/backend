package org.swyp.dessertbee.store.store.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.store.dto.response.StoreImageResponse;
import org.swyp.dessertbee.store.store.entity.Store;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        String folder = "store/" + store.getStoreId();

        // 삭제할 이미지 ID가 있다면 먼저 삭제
        List<Long> safeDeleteImageIds = toSafeLongList(deleteImageIds);
        if (!safeDeleteImageIds.isEmpty()) {
            imageService.deleteImagesByIds(safeDeleteImageIds);
        }

        // 새로운 이미지가 있다면 업로드
        if (storeImageFiles != null && !storeImageFiles.isEmpty()) {
            imageService.uploadAndSaveImages(storeImageFiles, ImageType.STORE, store.getStoreId(), folder);
        }
    }

    /**
     * 사장님 픽 이미지 처리 메서드
     */
    @Override
    public void updateOwnerPickImages(Store store, List<MultipartFile> ownerPickImageFiles, List<Long> deleteImageIds) {
        String folder = "ownerpick/" + store.getStoreId();

        // 삭제할 이미지 ID가 있다면 먼저 삭제
        List<Long> safeDeleteImageIds = toSafeLongList(deleteImageIds);
        if (!safeDeleteImageIds.isEmpty()) {
            imageService.deleteImagesByIds(safeDeleteImageIds);
        }

        // 새로운 이미지가 있다면 업로드
        if (ownerPickImageFiles != null && !ownerPickImageFiles.isEmpty()) {
            imageService.uploadAndSaveImages(ownerPickImageFiles, ImageType.OWNERPICK, store.getStoreId(), folder);
        }
    }

    /**
     * 가게 대표 이미지 조회 메서드
     */
    @Override
    public List<StoreImageResponse> getStoreImages(Long storeId) {
        return imageService.getStoreImagesWithIdByTypeAndId(ImageType.STORE, storeId);
    }

    /**
     * 사장님 픽 이미지 조회 메서드
     */
    @Override
    public List<StoreImageResponse> getOwnerPickImages(Long storeId) {
        return imageService.getStoreImagesWithIdByTypeAndId(ImageType.OWNERPICK, storeId);
    }


    /**
     * 안전한 Long 리스트로 변환 (null이나 Integer가 섞여있는 경우 처리)
     */
    private List<Long> toSafeLongList(List<Long> ids) {
        if (ids == null) return List.of();

        return ids.stream()
                .filter(Objects::nonNull)
                .map(id -> id instanceof Long ? id : Long.valueOf(id.toString()))
                .collect(Collectors.toList());
    }

    /**
     * 여러 가게의 대표 이미지 배치 조회
     */
    public Map<Long, List<StoreImageResponse>> getStoreImagesBatch(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return imageService.getStoreImagesByIds(ImageType.STORE, storeIds);
    }

    /**
     * 여러 가게의 업주 선택 이미지 배치 조회
     */
    public Map<Long, List<StoreImageResponse>> getOwnerPickImagesBatch(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return imageService.getStoreImagesByIds(ImageType.OWNERPICK, storeIds);
    }

    /**
     * 가게 이미지 URL만 배치 조회
     */
    public Map<Long, List<String>> getStoreImageUrlsBatch(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return imageService.getImageUrlsByIds(ImageType.STORE, storeIds);
    }
}
