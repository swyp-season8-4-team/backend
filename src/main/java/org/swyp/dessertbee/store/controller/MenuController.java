package org.swyp.dessertbee.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.store.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.dto.response.MenuResponse;
import org.swyp.dessertbee.store.service.MenuService;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /** 특정 가게의 메뉴 목록 조회 */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<MenuResponse>> getMenusByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(menuService.getMenusByStore(storeId));
    }

    /** 특정 가게의 특정 메뉴 조회 */
    @GetMapping("{menuId}")
    public ResponseEntity<MenuResponse> getMenuByStore(
            @PathVariable Long storeId,
            @PathVariable Long menuId) {
        return ResponseEntity.ok(menuService.getMenuByStore(storeId, menuId));
    }

    /** 메뉴 추가 */
    @PostMapping("/{storeId}")
    public ResponseEntity<Void> addMenu(@PathVariable Long storeId, @RequestBody MenuCreateRequest request){
        menuService.addMenu(storeId, request);
        return ResponseEntity.ok().build();
    }

    /** 메뉴 수정 */
    @PutMapping("/{menuId}")
    public ResponseEntity<Void> updateMenu(@PathVariable Long menuId, @RequestBody MenuCreateRequest request){
        menuService.updateMenu(menuId, request);
        return ResponseEntity.ok().build();
    }

    /** 메뉴 삭제 */
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long menuId){
        menuService.deleteMenu((menuId));
        return ResponseEntity.ok().build();
    }
}
