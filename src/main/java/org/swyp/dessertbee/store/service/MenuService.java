package org.swyp.dessertbee.store.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.store.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.dto.response.MenuResponse;
import org.swyp.dessertbee.store.entity.Menu;
import org.swyp.dessertbee.store.repository.MenuRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private final MenuRepository menuRepository;
    private final ImageService imageService;

    /** 특정 가게의 메뉴 목록 조회 */
    public List<MenuResponse> getMenusByStore(Long storeId) {
        return menuRepository.findByStoreIdAndDeletedAtIsNull(storeId).stream()
                .map(menu -> MenuResponse.fromEntity(menu, imageService.getImagesByTypeAndId(ImageType.MENU, menu.getId())))
                .collect(Collectors.toList());
    }

    /** 특정 가게의 특정 메뉴 조회 */
    public MenuResponse getMenuByStore(Long storeId, Long menuId) {
        Menu menu = menuRepository.findByIdAndStoreIdAndDeletedAtIsNull(menuId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 가게에 존재하지 않는 메뉴입니다."));

        return MenuResponse.fromEntity(menu, imageService.getImagesByTypeAndId(ImageType.MENU, menuId));
    }

    /** 메뉴 추가 */
    public void addMenu(Long storeId, MenuCreateRequest request, MultipartFile file) {
        if (menuRepository.existsByStoreIdAndNameAndDeletedAtIsNull(storeId, request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 메뉴입니다.");
        }

        Menu menu = menuRepository.save(Menu.builder()
                .storeId(storeId)
                .name(request.getName())
                .price(request.getPrice())
                .isPopular(request.getIsPopular())
                .description(request.getDescription())
                .build());

        if (file != null) {
            imageService.uploadAndSaveImage(file, ImageType.MENU, menu.getId(), "menu/" + menu.getId());
        }
    }

    /** 여러 개의 메뉴 추가 */
    public void addMenus(Long storeId, List<MenuCreateRequest> menuRequests, Map<String, MultipartFile> menuImageFiles) {
        if (menuRequests == null || menuRequests.isEmpty()) return;

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
            MultipartFile file = menuImageFiles.get(menu.getName()); // 수정된 부분 (이름 기반 매핑)
            if (file != null) {
                imageService.uploadAndSaveImage(file, ImageType.MENU, menu.getId(), "menu/" + menu.getId());
            }
        });
    }


    /** 메뉴 수정 */
    public void updateMenu(Long storeId, Long menuId, MenuCreateRequest request, MultipartFile file) {
        Menu menu = menuRepository.findByIdAndStoreIdAndDeletedAtIsNull(menuId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));

        menu.update(request.getName(), request.getPrice(), request.getIsPopular(), request.getDescription());

        // 기존 이미지 삭제 후 새 이미지 업로드
        if (file != null) {
            imageService.updateImage(ImageType.MENU, menuId, file, "menu/" + menuId);
        }
    }

    /** 메뉴 삭제 */
    public void deleteMenu(Long storeId, Long menuId) {
        Menu menu = menuRepository.findByIdAndStoreIdAndDeletedAtIsNull(menuId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));

        menu.softDelete();
        menuRepository.save(menu);
        imageService.deleteImagesByRefId(ImageType.MENU, menuId);
    }
}