package org.swyp.dessertbee.store.store.handler;

import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.store.entity.Store;

import java.util.List;
import java.util.Map;

public interface StoreMenuHandler {
    /** 메뉴 이미지 처리 */
    Map<String, MultipartFile> createMenuImageMap(List<MultipartFile> menuImageFiles);
    /** 메뉴 처리 */
    <T> void processMenus(Store store, List<T> menuRequests, List<MultipartFile> menuImageFiles);
}
