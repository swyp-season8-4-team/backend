package org.swyp.dessertbee.store.store.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.store.review.dto.response.StoreReviewResponse;
import org.swyp.dessertbee.store.store.exception.StoreExceptions.*;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.store.store.dto.response.*;
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
    @Operation(summary = "저장 리스트 전체 조회 (completed)", description = "유저의 모든 가게저장 리스트를 조회합니다.")
    @ApiResponse( responseCode = "200", description = "저장 리스트 전체 조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserStoreListResponse.class))))
    @ApiErrorResponses({ErrorCode.USER_NOT_FOUND, ErrorCode.USER_STORE_SERVICE_ERROR, ErrorCode.INVALID_USER_UUID})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @GetMapping("/{userUuid}/lists")
    public ResponseEntity<List<UserStoreListResponse>> getUserStoreLists(@PathVariable UUID userUuid) {
        return ResponseEntity.ok(userStoreService.getUserStoreLists(userUuid));
    }

    /** 저장 리스트 생성 */
    @Operation(summary = "저장 리스트 생성 (completed)", description = "유저의 가게 저장 리스트를 생성합니다.")
    @ApiResponse( responseCode = "200", description = "저장 리스트 생성 성", content = @Content(schema = @Schema(implementation = UserStoreListResponse.class)))
    @ApiErrorResponses({ErrorCode.USER_NOT_FOUND, ErrorCode.USER_STORE_SERVICE_ERROR, ErrorCode.INVALID_USER_UUID, ErrorCode.STORE_LIST_CREATION_FAILED})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @PostMapping("/{userUuid}/lists")
    public ResponseEntity<UserStoreListResponse> createUserStoreList(@PathVariable UUID userUuid,
                                                             @RequestParam String listName,
                                                             @RequestParam Long iconColorId) {
        return ResponseEntity.ok(userStoreService.createUserStoreList(userUuid, listName, iconColorId));
    }

    /** listId로 특정 리스트 정보 조회 */
    @Operation(summary = "저장 리스트 정보 조회 (completed)", description = "저장 리스트 정보를 조회합니다.")
    @ApiResponse( responseCode = "200", description = "저장 리스트 정보 조회 성공", content = @Content(schema = @Schema(implementation = UserStoreListSimpleResponse.class)))
    @ApiErrorResponses({ErrorCode.USER_STORE_SERVICE_ERROR, ErrorCode.STORE_LIST_NOT_FOUND})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @GetMapping("/lists/{listId}")
    public ResponseEntity<UserStoreListSimpleResponse> getUserStoreList(@PathVariable Long listId) {
        return ResponseEntity.ok(userStoreService.getUserStoreList(listId));
    }

    /** 저장 리스트 수정 */
    @Operation(summary = "저장 리스트 수정 (completed)", description = "저장 리스트 정보를 수정합니다.")
    @ApiResponse( responseCode = "200", description = "저장 리스트 정보 수정 성공", content = @Content(schema = @Schema(implementation = UserStoreListResponse.class)))
    @ApiErrorResponses({ErrorCode.USER_STORE_SERVICE_ERROR, ErrorCode.STORE_LIST_NOT_FOUND, ErrorCode.STORE_LIST_UPDATE_FAILED})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @PatchMapping("/lists/{listId}")
    public ResponseEntity<UserStoreListResponse> updateUserStoreList(@PathVariable Long listId,
                                                             @RequestParam String newName,
                                                             @RequestParam Long newIconColorId) {
        return ResponseEntity.ok(userStoreService.updateUserStoreList(listId, newName, newIconColorId));
    }

    /** 저장 리스트 삭제 */
    @Operation(summary = "저장 리스트 삭제 (completed)", description = "저장 리스트를 삭제합니다.")
    @ApiResponse( responseCode = "204", description = "저장 리스트 삭제 성공")
    @ApiErrorResponses({ErrorCode.USER_STORE_SERVICE_ERROR, ErrorCode.STORE_LIST_NOT_FOUND, ErrorCode.STORE_LIST_DELETE_FAILED})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @DeleteMapping("/lists/{listId}")
    public ResponseEntity<Void> deleteUserStoreList(@PathVariable Long listId) {
        userStoreService.deleteUserStoreList(listId);
        return ResponseEntity.noContent().build();
    }

    /** 리스트에 가게 추가 */
    @Operation(summary = "리스트에 가게 저장 (completed)", description = "해당 리스트에 가게를 저장합니다.")
    @ApiResponse( responseCode = "200", description = "리스트에 가게 저장 성공", content = @Content(schema = @Schema(implementation = SavedStoreResponse.class)))
    @ApiErrorResponses({ErrorCode.USER_STORE_SERVICE_ERROR, ErrorCode.STORE_LIST_NOT_FOUND, ErrorCode.STORE_SAVE_FAILED, ErrorCode.STORE_NOT_FOUND, ErrorCode.INVALID_STORE_UUID})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @PostMapping("/lists/{listId}/stores/{storeUuid}")
    public ResponseEntity<SavedStoreResponse> addStoreToList(@PathVariable Long listId, @PathVariable String storeUuid, @RequestBody List<Long> userPreferences) {
        try {
            UUID uuid = UUID.fromString(storeUuid); // 수동 변환
            return ResponseEntity.ok(userStoreService.addStoreToList(listId, uuid, userPreferences));
        } catch (IllegalArgumentException e) {
            throw new InvalidStoreUuidException();
        }
    }

    /** 리스트별 저장된 가게 위치 조회 */
    @Operation(summary = "저장 리스트 내 가게 위치 조회 (completed)", description = "특정 리스트에 저장된 가게들의 storeId, name, latitude, longitude, listId, iconColorId를 반환합니다.")
    @ApiResponse( responseCode = "200", description = "저장 리스트 내 가게 위치 조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = StoreListLocationResponse.class))))
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @GetMapping("/lists/{listId}/stores/locations")
    public ResponseEntity<List<StoreListLocationResponse>> getStoresByListId(@PathVariable Long listId) {
        return ResponseEntity.ok(userStoreService.getStoresByListId(listId));
    }

    /** 리스트별 저장된 가게 조회 */
    @Operation(summary = "리스트에 저장된 가게 조회 (completed)", description = "해당 리스트에 저장된 가게를 조회합니다.")
    @ApiResponse( responseCode = "200", description = "리스트에 저장된 가게 조회 성공", content = @Content(schema = @Schema(implementation = UserStoreListResponse.class)))
    @ApiErrorResponses({ErrorCode.USER_STORE_SERVICE_ERROR, ErrorCode.STORE_LIST_NOT_FOUND})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @GetMapping("/lists/{listId}/stores")
    public ResponseEntity<UserStoreListResponse> getStoresByList(@PathVariable Long listId) {
        return ResponseEntity.ok(userStoreService.getStoresByList(listId));
    }

    /** 리스트에서 가게 삭제 */
    @Operation(summary = "리스트에 저장된 가게 삭제 (completed)", description = "해당 리스트에 저장된 가게를 삭제합니다.")
    @ApiResponse( responseCode = "204", description = "리스트에 저장된 가게 삭제 성공")
    @ApiErrorResponses({ErrorCode.USER_STORE_SERVICE_ERROR, ErrorCode.STORE_LIST_NOT_FOUND, ErrorCode.STORE_NOT_FOUND, ErrorCode.SAVED_STORE_NOT_FOUND, ErrorCode.SAVED_STORE_DELETE_FAILED})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
    @DeleteMapping("/lists/{listId}/stores/{storeUuid}")
    public ResponseEntity<Void> removeStoreFromList(@PathVariable Long listId, @PathVariable UUID storeUuid) {
        userStoreService.removeStoreFromList(listId, storeUuid);
        return ResponseEntity.noContent().build();
    }
}
