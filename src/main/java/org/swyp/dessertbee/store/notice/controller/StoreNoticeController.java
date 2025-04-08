package org.swyp.dessertbee.store.notice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.store.notice.dto.request.StoreNoticeRequest;
import org.swyp.dessertbee.store.notice.dto.response.StoreNoticeResponse;
import org.swyp.dessertbee.store.notice.service.StoreNoticeService;

import java.util.List;
import java.util.UUID;

@Tag(name = "StoreNotice", description = "가게 공지 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/stores/{storeUuid}/notices")
@RequiredArgsConstructor
public class StoreNoticeController {

    private StoreNoticeService storeNoticeService;

    /** 공지 추가 */
    @Operation(summary = "공지 추가 (completed)", description = "가게 공지를 추가합니다.")
    @ApiResponse(responseCode = "200", description = "공지 추가 성공")
    @ApiErrorResponses({ErrorCode.INVALID_STORE_UUID, ErrorCode.STORE_NOTICE_SERVICE_ERROR, ErrorCode.STORE_NOTICE_CREATION_FAILED})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_OWNER')")
    @PostMapping
    public ResponseEntity<Void> createNotice(
            @PathVariable UUID storeUuid,
            @RequestBody StoreNoticeRequest request) {
        storeNoticeService.createNotice(storeUuid, request);
        return ResponseEntity.ok().build();
    }

    /** 공지 수정 */
    @Operation(summary = "공지 수정 (completed)", description = "특정 공지를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "공지 수정 성공", content = @Content(schema = @Schema(implementation = StoreNoticeResponse.class)))
    @ApiErrorResponses({ErrorCode.INVALID_STORE_UUID, ErrorCode.STORE_NOTICE_NOT_FOUND, ErrorCode.STORE_NOTICE_UPDATE_FAILED, ErrorCode.STORE_NOTICE_SERVICE_ERROR})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_OWNER')")
    @PatchMapping("/{noticeId}")
    public ResponseEntity<StoreNoticeResponse> updateNotice(
            @PathVariable UUID storeUuid,
            @PathVariable Long noticeId,
            @RequestBody StoreNoticeRequest request) {
        StoreNoticeResponse response = storeNoticeService.updateNotice(storeUuid, noticeId, request);
        return ResponseEntity.ok(response);
    }

    /** 공지 삭제 */
    @Operation(summary = "공지 삭제 (completed)", description = "특정 공지를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "공지 삭제 성공")
    @ApiErrorResponses({ErrorCode.INVALID_STORE_UUID, ErrorCode.STORE_NOTICE_NOT_FOUND, ErrorCode.STORE_NOTICE_DELETE_FAILED, ErrorCode.STORE_NOTICE_SERVICE_ERROR})
    @PreAuthorize("isAuthenticated() and hasRole('ROLE_OWNER')")
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable UUID storeUuid,
            @PathVariable Long noticeId) {
        storeNoticeService.deleteNotice(storeUuid, noticeId);
        return ResponseEntity.noContent().build();
    }

    /** 특정 공지 조회 */
    @Operation(summary = "공지 조회 (completed)", description = "공지 ID로 특정 공지를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공지 조회 성공", content = @Content(schema = @Schema(implementation = StoreNoticeResponse.class)))
    @ApiErrorResponses({ErrorCode.STORE_NOTICE_NOT_FOUND, ErrorCode.STORE_NOTICE_SERVICE_ERROR})
    @GetMapping("/{noticeId}")
    public ResponseEntity<StoreNoticeResponse> getNotice(
            @PathVariable Long storeUuid,
            @PathVariable Long noticeId) {
        StoreNoticeResponse response = storeNoticeService.getNotice(noticeId);
        return ResponseEntity.ok(response);
    }

    /** 전체 공지 조회 */
    @Operation(summary = "공지 리스트 조회 (completed)", description = "가게의 전체 공지를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공지 리스트 조회 성공", content = @Content(schema = @Schema(implementation = StoreNoticeResponse.class)))
    @ApiErrorResponses({ErrorCode.INVALID_STORE_UUID, ErrorCode.STORE_NOTICE_SERVICE_ERROR})
    @GetMapping
    public ResponseEntity<List<StoreNoticeResponse>> getNoticesByStore(
            @PathVariable UUID storeUuid) {
        List<StoreNoticeResponse> responses = storeNoticeService.getNoticesByStoreUuid(storeUuid);
        return ResponseEntity.ok(responses);
    }
}
