package org.swyp.dessertbee.store.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.store.store.dto.response.SavedStoreResponse;
import org.swyp.dessertbee.store.store.dto.response.UserStoreListResponse;
import org.swyp.dessertbee.store.store.entity.SavedStore;
import org.swyp.dessertbee.store.store.entity.UserStoreList;
import org.swyp.dessertbee.store.store.service.UserStoreService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-store")
@RequiredArgsConstructor
public class UserStoreController {

    private final UserStoreService userStoreService;

    /** 저장 리스트 전체 조회 */
    @GetMapping("/{userUuid}/lists")
    public ResponseEntity<List<UserStoreListResponse>> getUserStoreLists(@PathVariable UUID userUuid) {
        return ResponseEntity.ok(userStoreService.getUserStoreLists(userUuid));
    }

    /** 저장 리스트 생성 */
    @PostMapping("/{userUuid}/lists")
    public ResponseEntity<UserStoreListResponse> createUserStoreList(@PathVariable UUID userUuid,
                                                             @RequestParam String listName,
                                                             @RequestParam Long iconColorId) {
        return ResponseEntity.ok(userStoreService.createUserStoreList(userUuid, listName, iconColorId));
    }

    /** 저장 리스트 수정 */
    @PutMapping("/lists/{listId}")
    public ResponseEntity<UserStoreListResponse> updateUserStoreList(@PathVariable Long listId,
                                                             @RequestParam String newName,
                                                             @RequestParam Long newIconColorId) {
        return ResponseEntity.ok(userStoreService.updateUserStoreList(listId, newName, newIconColorId));
    }

    /** 저장 리스트 삭제 */
    @DeleteMapping("/lists/{listId}")
    public ResponseEntity<Void> deleteUserStoreList(@PathVariable Long listId) {
        userStoreService.deleteUserStoreList(listId);
        return ResponseEntity.noContent().build();
    }

    /** 리스트에 가게 추가 */
    @PostMapping("/lists/{listId}/stores/{storeUuid}")
    public ResponseEntity<SavedStoreResponse> addStoreToList(@PathVariable Long listId, @PathVariable UUID storeUuid) {
        return ResponseEntity.ok(userStoreService.addStoreToList(listId, storeUuid));
    }

    /** 리스트별 저장된 가게 조회 */
    @GetMapping("/lists/{listId}/stores")
    public ResponseEntity<List<SavedStoreResponse>> getStoresByList(@PathVariable Long listId) {
        return ResponseEntity.ok(userStoreService.getStoresByList(listId));
    }

    /** 리스트에서 가게 삭제 */
    @DeleteMapping("/lists/{listId}/stores/{storeUuid}")
    public ResponseEntity<Void> removeStoreFromList(@PathVariable Long listId, @PathVariable UUID storeUuid) {
        userStoreService.removeStoreFromList(listId, storeUuid);
        return ResponseEntity.noContent().build();
    }
}
