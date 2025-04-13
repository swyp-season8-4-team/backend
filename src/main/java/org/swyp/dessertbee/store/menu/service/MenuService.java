package org.swyp.dessertbee.store.menu.service;

import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.menu.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.menu.dto.response.MenuResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MenuService {
    /** 가게의 메뉴 이름들 불러오기 (검색용) */
    List<String> getMenuNames(Long storeId);
    /** 특정 가게의 메뉴 목록 조회 */
    List<MenuResponse> getMenusByStore(UUID storeUuid);

    /** 특정 가게의 특정 메뉴 조회 */
    MenuResponse getMenuByStore(UUID storeUuid, UUID menuUuid);

    /** 개별 메뉴 추가 (개별 메뉴 수정/추가 API 용) */
    void addMenu(UUID storeUuid, MenuCreateRequest request, MultipartFile file);

    /** 메뉴 전체 일괄 추가 (가게 등록 시 사용) */
    void addMenus(UUID storeUuid, List<MenuCreateRequest> menuRequests, Map<String, MultipartFile> menuImageFiles);

    /** 메뉴 수정 */
    void updateMenu(UUID storeUuid, UUID menuUuid, MenuCreateRequest request, MultipartFile file);

    /** 메뉴 삭제 */
    void deleteMenu(UUID storeUuid, UUID menuUuid);
}
