package org.swyp.dessertbee.store.menu.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.menu.dto.request.MenuCreateRequest;
import org.swyp.dessertbee.store.menu.dto.response.MenuResponse;
import org.swyp.dessertbee.store.menu.service.MenuService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stores/{storeUuid}/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /** 특정 가게의 메뉴 목록 조회 */
    @GetMapping
    public ResponseEntity<List<MenuResponse>> getMenusByStore(@PathVariable UUID storeUuid) {
        return ResponseEntity.ok(menuService.getMenusByStore(storeUuid));
    }

    /** 특정 가게의 특정 메뉴 조회 */
    @GetMapping("/{menuUuid}")
    public ResponseEntity<MenuResponse> getMenuByStore(
            @PathVariable UUID storeUuid,
            @PathVariable UUID menuUuid) {
        return ResponseEntity.ok(menuService.getMenuByStore(storeUuid, menuUuid));
    }

    /** 메뉴 등록 (파일 업로드 포함) */
    @PostMapping
    public ResponseEntity<Void> addMenus(
            @PathVariable UUID storeUuid,
            @RequestPart("requests") String requestJson,
            @RequestPart(value = "menuImages", required = false) List<MultipartFile> menuImageFiles) {

        // JSON을 List<MenuCreateRequest>로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        List<MenuCreateRequest> menuRequests;
        try {
            // 단일 객체 처리
            if (requestJson.trim().startsWith("{")) {
                MenuCreateRequest singleRequest = objectMapper.readValue(requestJson, MenuCreateRequest.class);
                menuRequests = Collections.singletonList(singleRequest);
            } else {
                // 리스트 처리
                menuRequests = objectMapper.readValue(requestJson, new TypeReference<>() {});
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 파싱 오류", e);
        }

        if (menuRequests.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // **파일 업로드 단일/다중 지원**
        Map<String, MultipartFile> menuImageMap = menuImageFiles != null
                ? menuImageFiles.stream().collect(Collectors.toMap(MultipartFile::getOriginalFilename, file -> file, (a, b) -> a))
                : Collections.emptyMap();

        menuService.addMenus(storeUuid, menuRequests, menuImageMap);
        return ResponseEntity.ok().build();
    }

    /** 메뉴 수정 (파일 업로드 포함) */
    @PatchMapping("/{menuUuid}")
    public ResponseEntity<Void> updateMenu(
            @PathVariable UUID storeUuid,
            @PathVariable UUID menuUuid,
            @RequestParam("request") String requestJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        // JSON 데이터를 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        MenuCreateRequest request;
        try {
            request = objectMapper.readValue(requestJson, MenuCreateRequest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 파싱 오류", e);
        }

        menuService.updateMenu(storeUuid, menuUuid, request, file);
        return ResponseEntity.ok().build();
    }

    /** 메뉴 삭제 */
    @DeleteMapping("/{menuUuid}")
    public ResponseEntity<Void> deleteMenu(@PathVariable UUID storeUuid, @PathVariable UUID menuUuid) {
        menuService.deleteMenu(storeUuid, menuUuid);
        return ResponseEntity.ok().build();
    }
}
