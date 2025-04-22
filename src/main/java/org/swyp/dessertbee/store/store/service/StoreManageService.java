package org.swyp.dessertbee.store.store.service;

import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.store.dto.request.StoreCreateRequest;
import org.swyp.dessertbee.store.store.dto.request.StoreUpdateRequest;
import org.swyp.dessertbee.store.store.dto.response.StoreInfoResponse;

import java.util.List;
import java.util.UUID;

public interface StoreManageService {
    /** 가게 등록 (이벤트, 쿠폰, 메뉴 + 이미지 포함) */
    void createStore(StoreCreateRequest request,
                     List<MultipartFile> storeImageFiles,
                     List<MultipartFile> ownerPickImageFiles,
                     List<MultipartFile> menuImageFiles);

    /** 가게 수정 */
    StoreInfoResponse updateStore(UUID storeUuid,
                                  StoreUpdateRequest request,
                                  List<MultipartFile> storeImageFiles,
                                  List<MultipartFile> ownerPickImageFiles);

    /** 가게 정보 조회 */
    StoreInfoResponse getStoreInfo(UUID storeUuid);

    /** 가게 삭제 */
    void deleteStore(UUID storeUuid);
}
