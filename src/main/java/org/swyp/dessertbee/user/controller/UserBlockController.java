package org.swyp.dessertbee.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.user.dto.request.UserBlockRequest;
import org.swyp.dessertbee.user.dto.response.UserBlockCheckResponse;
import org.swyp.dessertbee.user.dto.response.UserBlockResponse;
import org.swyp.dessertbee.user.service.UserBlockService;
import org.swyp.dessertbee.user.service.UserService;

import java.util.UUID;

/**
 * 사용자 차단 관련 컨트롤러
 */
@Tag(name = "UserBlock", description = "사용자 차단 관련 API")
@RestController
@RequestMapping("/api/users/blocks")
@RequiredArgsConstructor
@Slf4j
public class UserBlockController {

    private final UserBlockService userBlockService;
    private final UserService userService;

    /**
     * 사용자 차단하기
     */
    @Operation(
            summary = "사용자 차단하기",
            description = "특정 사용자를 차단합니다."
    )
    @ApiResponse(
            responseCode = "201",
            description = "사용자 차단 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserBlockResponse.class)
            )
    )
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED_ACCESS,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.SELF_BLOCK_NOT_ALLOWED,
            ErrorCode.ALREADY_BLOCKED_USER
    })
    @PostMapping
    public ResponseEntity<UserBlockResponse> blockUser(@RequestBody UserBlockRequest request) {
        // 현재 로그인된 사용자 정보 가져오기
        UUID currentUserUuid = userService.getCurrentUser().getUserUuid();
        UserBlockResponse response = userBlockService.blockUser(currentUserUuid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사용자 차단 해제하기
     */
    @Operation(
            summary = "사용자 차단 해제하기",
            description = "차단한 사용자를 해제합니다."
    )
    @ApiResponse(responseCode = "204", description = "사용자 차단 해제 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED_ACCESS,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.USER_BLOCK_NOT_FOUND,
    })
    @DeleteMapping("/{blockId}")
    public ResponseEntity<Void> unblockUser(@PathVariable Long blockId) {
        // 현재 로그인된 사용자 정보 가져오기
        UUID currentUserUuid = userService.getCurrentUser().getUserUuid();
        userBlockService.unblockUser(currentUserUuid, blockId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 차단한 사용자 목록 조회
     */
    @Operation(
            summary = "차단한 사용자 목록 조회",
            description = "현재 사용자가 차단한 사용자 목록을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "차단 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserBlockResponse.ListResponse.class)
            )
    )
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.USER_NOT_FOUND})
    @GetMapping
    public ResponseEntity<UserBlockResponse.ListResponse> getBlockedUsers() {
        // 현재 로그인된 사용자 정보 가져오기
        UUID currentUserUuid = userService.getCurrentUser().getUserUuid();
        UserBlockResponse.ListResponse response = userBlockService.getBlockedUsers(currentUserUuid);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "차단 여부 확인",
            description = "특정 사용자를 차단했는지 여부와 차단 정보를 확인합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "차단 여부 확인 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserBlockCheckResponse.class)
            )
    )
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.USER_NOT_FOUND, ErrorCode.INVALID_USER_UUID})
    @GetMapping("/check/{blockedUserUuid}")
    public ResponseEntity<UserBlockCheckResponse> isBlocked(@PathVariable String blockedUserUuid) {
        // 현재 로그인된 사용자 정보 가져오기
        UUID currentUserUuid = userService.getCurrentUser().getUserUuid();
        UserBlockCheckResponse response = userBlockService.checkBlockStatus(
                currentUserUuid,
                UUID.fromString(blockedUserUuid)
        );
        return ResponseEntity.ok(response);
    }
}