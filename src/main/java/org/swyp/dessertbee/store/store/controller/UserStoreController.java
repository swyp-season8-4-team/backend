package org.swyp.dessertbee.store.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.store.store.dto.response.SavedStoreResponse;
import org.swyp.dessertbee.store.store.dto.response.UserStoreListResponse;
import org.swyp.dessertbee.store.store.dto.response.UserStoreListSimpleResponse;
import org.swyp.dessertbee.store.store.entity.SavedStore;
import org.swyp.dessertbee.store.store.entity.UserStoreList;
import org.swyp.dessertbee.store.store.service.UserStoreService;

import java.util.List;
import java.util.UUID;

@Tag(name = "UserStore", description = "저장된 가게 관련 API")
@RestController
@RequestMapping("/api/user-store")
@RequiredArgsConstructor
public class UserStoreController {

    private final UserStoreService userStoreService;

    /** 저장 리스트 전체 조회 */
    @Operation(summary = "저장 리스트 전체 조회", description = "유저의 모든 가게저장 리스트를 조회합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @GetMapping("/{userUuid}/lists")
    public ResponseEntity<List<UserStoreListResponse>> getUserStoreLists(@PathVariable UUID userUuid) {
        return ResponseEntity.ok(userStoreService.getUserStoreLists(userUuid));
    }

    /** 저장 리스트 생성 */
    @Operation(summary = "저장 리스트 생성", description = "유저의 가게 저장 리스트를 생성합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @PostMapping("/{userUuid}/lists")
    public ResponseEntity<UserStoreListResponse> createUserStoreList(@PathVariable UUID userUuid,
                                                             @RequestParam String listName,
                                                             @RequestParam Long iconColorId) {
        return ResponseEntity.ok(userStoreService.createUserStoreList(userUuid, listName, iconColorId));
    }

    /** listId로 특정 리스트 정보 조회 */
    @Operation(summary = "저장 리스트 정보 조회", description = "저장 리스트 정보를 조회합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @GetMapping("/lists/{listId}")
    public ResponseEntity<UserStoreListSimpleResponse> getUserStoreList(@PathVariable Long listId) {
        return ResponseEntity.ok(userStoreService.getUserStoreList(listId));
    }

    /** 저장 리스트 수정 */
    @Operation(summary = "저장 리스트 수정", description = "저장 리스트 정보를 수정합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @PatchMapping("/lists/{listId}")
    public ResponseEntity<UserStoreListResponse> updateUserStoreList(@PathVariable Long listId,
                                                             @RequestParam String newName,
                                                             @RequestParam Long newIconColorId) {
        return ResponseEntity.ok(userStoreService.updateUserStoreList(listId, newName, newIconColorId));
    }

    /** 저장 리스트 삭제 */
    @Operation(summary = "저장 리스트 삭제", description = "저장 리스트를 삭제합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @DeleteMapping("/lists/{listId}")
    public ResponseEntity<Void> deleteUserStoreList(@PathVariable Long listId) {
        userStoreService.deleteUserStoreList(listId);
        return ResponseEntity.noContent().build();
    }

    /** 리스트에 가게 추가 */
    @Operation(summary = "리스트에 가게 저장", description = "해당 리스트에 가게를 저장합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @PostMapping("/lists/{listId}/stores/{storeUuid}")
    public ResponseEntity<SavedStoreResponse> addStoreToList(@PathVariable Long listId, @PathVariable String storeUuid, @RequestBody List<Long> userPreferences) {
        try {
            UUID uuid = UUID.fromString(storeUuid); // 수동 변환
            return ResponseEntity.ok(userStoreService.addStoreToList(listId, uuid, userPreferences));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_STORE_UUID);
        }
    }

    /** 리스트별 저장된 가게 조회 */
    @Operation(summary = "리스트에 저장된 가게 조회", description = "해당 리스트에 저장된 가게를 조회합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @GetMapping("/lists/{listId}/stores")
    public ResponseEntity<UserStoreListResponse> getStoresByList(@PathVariable Long listId) {
        return ResponseEntity.ok(userStoreService.getStoresByList(listId));
    }

    /** 리스트에서 가게 삭제 */
    @Operation(summary = "리스트에 저장된 가게 삭제", description = "해당 리스트에 저장된 가게를 삭제합니다.")
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @DeleteMapping("/lists/{listId}/stores/{storeUuid}")
    public ResponseEntity<Void> removeStoreFromList(@PathVariable Long listId, @PathVariable UUID storeUuid) {
        userStoreService.removeStoreFromList(listId, storeUuid);
        return ResponseEntity.noContent().build();
    }
}
