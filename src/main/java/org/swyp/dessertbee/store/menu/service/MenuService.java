package org.swyp.dessertbee.store.menu.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.common.util.CustomMultipartFile;
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

    /** 파일명 재정의 */
    private MultipartFile renameFile(MultipartFile file, String menuName) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename.lastIndexOf('.') != -1) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String newFilename = menuName.trim() + extension;
        return new CustomMultipartFile(file, newFilename);
    }

    /** 특정 가게의 메뉴 목록 조회 */
    public List<MenuResponse> getMenusByStore(UUID storeUuid) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) {
            throw new BusinessException(ErrorCode.INVALID_STORE_UUID);
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
            throw new BusinessException(ErrorCode.INVALID_STORE_MENU_UUID);
        }
        Menu menu = menuRepository.findByMenuIdAndStoreIdAndDeletedAtIsNull(menuId, storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STORE_MENU));

        return MenuResponse.fromEntity(menu, imageService.getImagesByTypeAndId(ImageType.MENU, menuId));
    }

    /** 개별 메뉴 추가 (개별 메뉴 수정/추가 API 용) */
    public void addMenu(UUID storeUuid, MenuCreateRequest request, MultipartFile file) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) {
            throw new BusinessException(ErrorCode.INVALID_STORE_UUID);
        }

        Menu menu = Menu.builder()
                .storeId(storeId)
                .name(request.getName())
                .price(request.getPrice())
                .isPopular(request.getIsPopular())
                .description(request.getDescription())
                .build();
        menuRepository.save(menu);

        // 이미지 파일이 있는 경우 재정의된 파일명으로 업로드
        if (file != null) {
            MultipartFile renamedFile = renameFile(file, menu.getName());
            imageService.uploadAndSaveImage(renamedFile, ImageType.MENU, menu.getMenuId(), "menu/" + menu.getMenuId());
        }
    }

    /** 메뉴 전체 일괄 추가 (가게 등록 시 사용) */
    public void addMenus(UUID storeUuid, List<MenuCreateRequest> menuRequests, Map<String, MultipartFile> menuImageFiles) {
        if (menuRequests == null || menuRequests.isEmpty()) return;

        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        if (storeId == null) {
            throw new BusinessException(ErrorCode.INVALID_STORE_UUID);
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

        // 각 메뉴에 대해 이미지 파일 업로드 처리
        for (int i = 0; i < menus.size(); i++) {
            Menu menu = menus.get(i);
            String imageKey = menuRequests.get(i).getImageFileKey();
            if (imageKey != null) {
                MultipartFile file = menuImageFiles.get(imageKey);
                if (file != null) {
                    MultipartFile renamedFile = renameFile(file, menu.getName());
                    imageService.uploadAndSaveImage(renamedFile, ImageType.MENU, menu.getMenuId(), "menu/" + menu.getMenuId());
                }
            }
        }
    }

    /** 메뉴 수정 */
    public void updateMenu(UUID storeUuid, UUID menuUuid, MenuCreateRequest request, MultipartFile file) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        Long menuId = menuRepository.findMenuIdByMenuUuid(menuUuid);
        Menu menu = menuRepository.findByMenuIdAndStoreIdAndDeletedAtIsNull(menuId, storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_MENU_NOT_FOUND));

        menu.update(request.getName(), request.getPrice(), request.getIsPopular(), request.getDescription());

        if (file != null) {
            MultipartFile renamedFile = renameFile(file, menu.getName());
            imageService.updateImage(ImageType.MENU, menuId, renamedFile, "menu/" + menuId);
        }
    }

    /** 메뉴 삭제 */
    public void deleteMenu(UUID storeUuid, UUID menuUuid) {
        Long storeId = storeRepository.findStoreIdByStoreUuid(storeUuid);
        Long menuId = menuRepository.findMenuIdByMenuUuid(menuUuid);
        Menu menu = menuRepository.findByMenuIdAndStoreIdAndDeletedAtIsNull(menuId, storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_MENU_NOT_FOUND));

        menu.softDelete();
        menuRepository.save(menu);
        imageService.deleteImagesByRefId(ImageType.MENU, menuId);
    }
}