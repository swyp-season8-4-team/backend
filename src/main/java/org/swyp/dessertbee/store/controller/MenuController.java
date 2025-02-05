package org.swyp.dessertbee.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.dto.response.MenuResponse;
import org.swyp.dessertbee.store.service.MenuService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stores/{storeId}/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /** 특정 가게의 메뉴 목록 조회 */
    @GetMapping
    public ResponseEntity<List<MenuResponse>> getMenusByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(menuService.getMenusByStore(storeId));
    }

    /** 특정 가게의 특정 메뉴 조회 */
    @GetMapping("/{menuId}")
    public ResponseEntity<MenuResponse> getMenuByStore(
            @PathVariable Long storeId,
            @PathVariable Long menuId) {
        return ResponseEntity.ok(menuService.getMenuByStore(storeId, menuId));
    }

    /** 단일 메뉴 추가 (파일 업로드 포함) */
    @PostMapping
    public ResponseEntity<Void> addMenu(
            @PathVariable Long storeId,
            @RequestPart("request") MenuCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        menuService.addMenu(storeId, request, file);
        return ResponseEntity.ok().build();
    }

    /** 여러 개의 메뉴 추가 (파일 업로드 포함) */
    @PostMapping("/bulk")
    public ResponseEntity<Void> addMenus(
            @PathVariable Long storeId,
            @RequestPart("requests") List<MenuCreateRequest> menuRequests,
            @RequestPart(value = "menuImages", required = false) Map<String, MultipartFile> menuImageFiles) {
        menuService.addMenus(storeId, menuRequests, menuImageFiles);
        return ResponseEntity.ok().build();
    }

    /** 메뉴 수정 (파일 업로드 포함) */
    @PutMapping("/{menuId}")
    public ResponseEntity<Void> updateMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestPart("request") MenuCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        menuService.updateMenu(storeId, menuId, request, file);
        return ResponseEntity.ok().build();
    }

    /** 메뉴 삭제 */
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long storeId, @PathVariable Long menuId) {
        menuService.deleteMenu(storeId, menuId);
        return ResponseEntity.ok().build();
    }
}
