package org.swyp.dessertbee.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.store.store.service.UserStoreService;
import org.swyp.dessertbee.user.dto.NicknameValidationRequestDto;
import org.swyp.dessertbee.user.dto.UserDetailResponseDto;
import org.swyp.dessertbee.user.dto.UserResponseDto;
import org.swyp.dessertbee.user.dto.UserUpdateRequestDto;
import org.swyp.dessertbee.user.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 사용자 정보 조회 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserStoreService userStoreService;

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
    @PatchMapping(value="/me")
    public ResponseEntity<UserDetailResponseDto> updateMyInfo(@RequestBody @Valid UserUpdateRequestDto updateRequest) {

        UserDetailResponseDto response = userService.updateMyInfo(updateRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 탈퇴 처리
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {
        log.debug("유저 소프트 삭제를 진행합니다.");
        userService.deleteMyAccount();
        return ResponseEntity.noContent().build();
    }

    /**
     * 닉네임 중복 검사
     * @return 사용 가능 여부
     */
    @PostMapping("/validate/nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@Valid @RequestBody NicknameValidationRequestDto request) {
        boolean isAvailable = userService.checkNicknameAvailability(request.getNickname(), request.getPurpose());

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);

        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 이미지 업데이트
     * @param image 업로드할 프로필 이미지 파일
     * @return 업데이트된 사용자 정보
     */
    @PostMapping(
            value = "/me/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserDetailResponseDto> updateProfileImage(
            @RequestPart("image") MultipartFile image) {
        log.info("프로필 이미지 업데이트 요청 - 파일명: {}", image.getOriginalFilename());
        UserDetailResponseDto response = userService.updateProfileImage(image);
        return ResponseEntity.ok(response);
    }

    /** 사용자의 취향을 업데이트하고 저장된 모든 가게 리스트의 취향도 변경 */
    @PatchMapping("/{userUuid}/preferences")
    public ResponseEntity<Void> updateUserPreferences(
            @PathVariable UUID userUuid,
            @RequestBody List<String> newUserPreferences) {

        userStoreService.updateUserPreferencesAndSavedStores(userUuid, newUserPreferences);
        return ResponseEntity.noContent().build();
    }
}