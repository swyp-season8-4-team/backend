package org.swyp.dessertbee.store.menu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.store.menu.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.menu.dto.response.MenuResponse;
import org.swyp.dessertbee.store.menu.service.MenuService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "Menu", description = "가게 메뉴 관련 API")
@RestController
@RequestMapping("/api/stores/{storeUuid}/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /** 특정 가게의 메뉴 목록 조회 */
    @Operation(summary = "메뉴 목록 조회 (completed)", description = "가게의 메뉴 목록을 조회합니다.")
    @ApiResponse( responseCode = "200", description = "메뉴 목록 조회 성공", content = @Content(schema = @Schema(implementation = MenuResponse.class)))
    @ApiErrorResponses({ErrorCode.INVALID_STORE_UUID, ErrorCode.MENU_SERVICE_ERROR})
    @GetMapping
    public ResponseEntity<List<MenuResponse>> getMenusByStore(@PathVariable UUID storeUuid) {
        return ResponseEntity.ok(menuService.getMenusByStore(storeUuid));
    }

    /** 특정 가게의 특정 메뉴 조회 */
    @Operation(summary = "메뉴 정보 조회 (completed)", description = "메뉴 정보를 조회합니다.")
    @ApiResponse( responseCode = "200", description = "메뉴 정보 조회 성공", content = @Content(schema = @Schema(implementation = MenuResponse.class)))
    @ApiErrorResponses({ErrorCode.INVALID_STORE_MENU_UUID, ErrorCode.INVALID_STORE_MENU, ErrorCode.MENU_SERVICE_ERROR})
    @GetMapping("/{menuUuid}")
    public ResponseEntity<MenuResponse> getMenuByStore(
            @PathVariable UUID storeUuid,
            @PathVariable UUID menuUuid) {
        return ResponseEntity.ok(menuService.getMenuByStore(storeUuid, menuUuid));
    }

    /** 메뉴 등록 (파일 업로드 포함) */
    @Operation(summary = "메뉴 등록 (completed)", description = "가게에 메뉴를 등록합니다.")
    @ApiResponse( responseCode = "200", description = "메뉴 등록 성공")
    @ApiErrorResponses({ErrorCode.INVALID_STORE_UUID, ErrorCode.MENU_SERVICE_ERROR, ErrorCode.MENU_CREATION_FAILED})
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<Void> addMenus(
            @PathVariable UUID storeUuid,
            @RequestPart("requests") List<MenuCreateRequest> menuRequests,
            @RequestPart(value = "menuImages", required = false) List<MultipartFile> menuImageFiles) {

        if (menuRequests == null || menuRequests.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, MultipartFile> menuImageMap = menuImageFiles != null
                ? menuImageFiles.stream()
                .collect(Collectors.toMap(MultipartFile::getOriginalFilename, file -> file, (a, b) -> a))
                : Collections.emptyMap();

        menuService.addMenus(storeUuid, menuRequests, menuImageMap);
        return ResponseEntity.ok().build();
    }

    /** 메뉴 수정 (파일 업로드 포함) */
    @Operation(summary = "메뉴 수정 (completed)", description = "가게의 메뉴를 수정합니다.")
    @ApiResponse( responseCode = "200", description = "메뉴 수정 성공")
    @ApiErrorResponses({ErrorCode.STORE_MENU_NOT_FOUND, ErrorCode.MENU_SERVICE_ERROR, ErrorCode.MENU_UPDATE_FAILED})
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @PatchMapping("/{menuUuid}")
    public ResponseEntity<Void> updateMenu(
            @PathVariable UUID storeUuid,
            @PathVariable UUID menuUuid,
            @RequestParam("request") MenuCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        menuService.updateMenu(storeUuid, menuUuid, request, file);
        return ResponseEntity.ok().build();
    }

    /** 메뉴 삭제 */
    @Operation(summary = "메뉴 삭제 (completed)", description = "가게의 메뉴를 삭제합니다.")
    @ApiResponse( responseCode = "200", description = "메뉴 삭제 성공")
    @ApiErrorResponses({ErrorCode.STORE_MENU_NOT_FOUND, ErrorCode.MENU_SERVICE_ERROR, ErrorCode.MENU_DELETE_FAILED})
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_OWNER', 'ROLE_ADMIN')")
    @DeleteMapping("/{menuUuid}")
    public ResponseEntity<Void> deleteMenu(@PathVariable UUID storeUuid, @PathVariable UUID menuUuid) {
        menuService.deleteMenu(storeUuid, menuUuid);
        return ResponseEntity.ok().build();
    }
}
