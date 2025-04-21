package org.swyp.dessertbee.store.store.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.menu.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.menu.service.MenuService;
import org.swyp.dessertbee.store.store.entity.Store;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreMenuHandlerImpl implements StoreMenuHandler {

    private final MenuService menuService;

    /**
     * 메뉴 이미지 맵 생성 유틸리티 메서드
     */
    @Override
    public Map<String, MultipartFile> createMenuImageMap(List<MultipartFile> menuImageFiles) {
        if (menuImageFiles == null) {
            return Collections.emptyMap();
        }
        return menuImageFiles.stream()
                .collect(Collectors.toMap(MultipartFile::getOriginalFilename, file -> file, (a, b) -> b));
    }

    /**
     * 메뉴 처리 메서드
     */
    @Override
    public  <T> void processMenus(Store store, List<T> menuRequests, List<MultipartFile> menuImageFiles) {
        if (menuRequests != null && !menuRequests.isEmpty()) {
            Map<String, MultipartFile> menuImageMap = createMenuImageMap(menuImageFiles);

            if (menuRequests.get(0) instanceof MenuCreateRequest) {
                @SuppressWarnings("unchecked")
                List<MenuCreateRequest> typedRequests = (List<MenuCreateRequest>) menuRequests;
                menuService.addMenus(store.getStoreUuid(), typedRequests, menuImageMap);
            }
        }
    }

}
