package org.swyp.dessertbee.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.user.dto.UserDetailResponseDto;
import org.swyp.dessertbee.user.dto.UserResponseDto;
import org.swyp.dessertbee.user.dto.UserUpdateRequestDto;
import org.swyp.dessertbee.user.service.UserService;

/**
 * 사용자 정보 조회 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 현재 인증된 사용자의 상세 정보를 조회
     * @return 사용자 상세 정보
     */
    @GetMapping("/me")
    public ResponseEntity<UserDetailResponseDto> getMyUserInfo() {
        UserDetailResponseDto response = userService.getMyUserInfo();
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 사용자의 기본 정보를 조회
     * @param userUuid 조회할 사용자의 UUID
     * @return 사용자 기본 정보
     */
    @GetMapping("/{userUuid}")
    public ResponseEntity<UserResponseDto> getUserInfo(@PathVariable String userUuid) {
        UserResponseDto response = userService.getUserInfo(userUuid);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 인증된 사용자의 정보를 수정합니다.
     * @return 사용자 상세 정보
     */
    @PatchMapping("/me")
    public ResponseEntity<UserDetailResponseDto> updateMyInfo(@RequestBody @Valid UserUpdateRequestDto updateRequest) {
        UserDetailResponseDto response = userService.updateMyInfo(updateRequest);
        return ResponseEntity.ok(response);
    }
}