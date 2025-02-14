package org.swyp.dessertbee.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.user.dto.NicknameValidationRequestDto;
import org.swyp.dessertbee.user.dto.UserDetailResponseDto;
import org.swyp.dessertbee.user.dto.UserResponseDto;
import org.swyp.dessertbee.user.dto.UserUpdateRequestDto;
import org.swyp.dessertbee.user.service.UserService;

import java.util.HashMap;
import java.util.Map;

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
    @PatchMapping(value="/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDetailResponseDto> updateMyInfo(@Valid @RequestPart(value = "data", required = false) UserUpdateRequestDto updateRequest,
                                                              @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        // 최소한 하나의 업데이트 데이터가 있는지 검증
        if (updateRequest == null && (profileImage == null || profileImage.isEmpty())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "업데이트할 정보가 없습니다.");
        }

        // null인 경우 빈 DTO 생성
        if (updateRequest == null) {
            updateRequest = UserUpdateRequestDto.builder().build();
        }

        UserDetailResponseDto response = userService.updateMyInfo(updateRequest, profileImage);
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
}