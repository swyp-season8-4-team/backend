package org.swyp.dessertbee.store.menu.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.menu.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.menu.dto.response.MenuResponse;
import org.swyp.dessertbee.store.menu.entity.Menu;
import org.swyp.dessertbee.store.menu.repository.MenuRepository;
import org.swyp.dessertbee.store.store.repository.StoreRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final ImageService imageService;
    private final StoreRepository storeRepository;

    /** 특정 가게의 메뉴 목록 조회 */
    public List<MenuResponse> getMenusByStore(UUID storeUuid) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) {
            throw new IllegalArgumentException("storeUuid에 해당하는 storeId를 찾을 수 없습니다: " + storeUuid);
        }

        return menuRepository.findByStoreIdAndDeletedAtIsNull(storeId).stream()
                .map(menu -> MenuResponse.fromEntity(menu, imageService.getImagesByTypeAndId(ImageType.MENU, menu.getMenuId())))
                .collect(Collectors.toList());
    }

    /** 특정 가게의 특정 메뉴 조회 */
    public MenuResponse getMenuByStore(UUID storeUuid, UUID menuUuid) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        Long menuId = menuRepository.findMenuIdByMenuUuid(menuUuid);
        if (menuId == null) {
            throw new IllegalArgumentException("해당 메뉴 UUID에 대한 menuId를 찾을 수 없습니다: " + menuUuid);
        }
        Menu menu = menuRepository.findByMenuIdAndStoreIdAndDeletedAtIsNull(menuId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 가게에 존재하지 않는 메뉴입니다."));

        return MenuResponse.fromEntity(menu, imageService.getImagesByTypeAndId(ImageType.MENU, menuId));
    }

    /** 메뉴 추가 */
    public void addMenus(UUID storeUuid, List<MenuCreateRequest> menuRequests, Map<String, MultipartFile> menuImageFiles) {
        if (menuRequests == null || menuRequests.isEmpty()) return;

        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) {
            throw new IllegalArgumentException("해당 UUID의 가게가 존재하지 않습니다. storeUuid=" + storeUuid);
        }

        // 메뉴 저장
        List<Menu> menus = menuRequests.stream()
                .map(request -> Menu.builder()
                        .storeId(storeId)
                        .name(request.getName())
                        .price(request.getPrice())
                        .isPopular(request.getIsPopular())
                        .description(request.getDescription())
                        .build())
                .collect(Collectors.toList());

        menuRepository.saveAll(menus);

        // 각 메뉴의 이름을 기반으로 이미지 파일 업로드
        menus.forEach(menu -> {
            MultipartFile file = menuImageFiles.get(menu.getName());
            if (file != null) {
                String url = imageService.uploadAndSaveImage(file, ImageType.MENU, menu.getMenuId(), "menu/" + menu.getMenuId());
                log.info(url);
            }
        });
    }


    /** 메뉴 수정 */
    public void updateMenu(UUID storeUuid, UUID menuUuid, MenuCreateRequest request, MultipartFile file) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        Long menuId = menuRepository.findMenuIdByMenuUuid(menuUuid);
        Menu menu = menuRepository.findByMenuIdAndStoreIdAndDeletedAtIsNull(menuId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));

        menu.update(request.getName(), request.getPrice(), request.getIsPopular(), request.getDescription());

        // 기존 이미지 삭제 후 새 이미지 업로드
        if (file != null) {
            imageService.updateImage(ImageType.MENU, menuId, file, "menu/" + menuId);
        }
    }

    /** 메뉴 삭제 */
    public void deleteMenu(UUID storeUuid, UUID menuUuid) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        Long menuId = menuRepository.findMenuIdByMenuUuid(menuUuid);
        Menu menu = menuRepository.findByMenuIdAndStoreIdAndDeletedAtIsNull(menuId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));

        menu.softDelete();
        menuRepository.save(menu);
        imageService.deleteImagesByRefId(ImageType.MENU, menuId);
    }
}