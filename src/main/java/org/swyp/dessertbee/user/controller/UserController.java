package org.swyp.dessertbee.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.store.review.dto.response.UserReviewListResponse;
import org.swyp.dessertbee.store.review.service.StoreReviewService;
import org.swyp.dessertbee.user.dto.request.NicknameAvailabilityRequestDto;
import org.swyp.dessertbee.user.dto.response.NicknameAvailabilityResponseDto;
import org.swyp.dessertbee.user.dto.response.UserDetailResponseDto;
import org.swyp.dessertbee.user.dto.response.UserResponseDto;
import org.swyp.dessertbee.user.dto.request.UserUpdateRequestDto;
import org.swyp.dessertbee.user.service.UserService;

/**
 * 사용자 정보 조회 관련 컨트롤러
 */
@Tag(name = "User", description = "사용자 정보 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final StoreReviewService storeReviewService;

    /**
     * 현재 인증된 사용자의 상세 정보를 조회
     * @return 사용자 상세 정보
     */
    @Operation(
            summary = "현재 사용자 정보 조회 (completed)",
            description = "현재 로그인된 사용자의 상세 정보를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "사용자 정보 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDetailResponseDto.class)
            )
    )
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.USER_NOT_FOUND})
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
    @Operation(
            summary = "특정 사용자 정보 조회 (completed)",
            description = "UUID로 특정 사용자의 기본 정보를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "사용자 정보 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDto.class)
            )
    )
    @ApiErrorResponses({ErrorCode.USER_NOT_FOUND, ErrorCode.INVALID_USER_UUID})
    @GetMapping("/{userUuid}")
    public ResponseEntity<UserResponseDto> getUserInfo(@PathVariable String userUuid) {
        UserResponseDto response = userService.getUserInfo(userUuid);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 인증된 사용자의 정보를 수정합니다.
     * @return 사용자 상세 정보
     */
    @Operation(
            summary = "사용자 정보 수정 (completed)",
            description = "현재 로그인된 사용자의 정보를 수정합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "사용자 정보 수정 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDetailResponseDto.class)
            )
    )
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.INVALID_INPUT_VALUE, ErrorCode.DUPLICATE_NICKNAME, ErrorCode.FORBIDDEN_OPERATION})
    @PatchMapping(value="/me")
    public ResponseEntity<UserDetailResponseDto> updateMyInfo(@RequestBody @Valid UserUpdateRequestDto updateRequest) {

        UserDetailResponseDto response = userService.updateMyInfo(updateRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 탈퇴 처리
     */
    @Operation(
            summary = "회원 탈퇴 (completed)",
            description = "현재 로그인된 사용자의 계정을 비활성화합니다."
    )
    @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공")
    @DeleteMapping("/me")
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.USER_NOT_FOUND})
    public ResponseEntity<Void> deleteMyAccount() {
        log.debug("유저 소프트 삭제를 진행합니다.");
        userService.deleteMyAccount();
        return ResponseEntity.noContent().build();
    }

    /**
     * 닉네임 중복 검사
     * @return 사용 가능 여부
     */
    @Operation(
            summary = "닉네임 중복 검사 (completed)",
            description = "닉네임 사용 가능 여부를 확인합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "닉네임 중복 검사 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NicknameAvailabilityResponseDto.class)
            )
    )
    @ApiErrorResponses({ErrorCode.INVALID_INPUT_VALUE})
    @PostMapping("/validate/nickname")
    public ResponseEntity<NicknameAvailabilityResponseDto> checkNickname(@Valid @RequestBody NicknameAvailabilityRequestDto request) {
        boolean isAvailable = userService.checkNicknameAvailability(request.getNickname(), request.getPurpose());

        NicknameAvailabilityResponseDto response = NicknameAvailabilityResponseDto.builder()
                .available(isAvailable)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 이미지 업데이트
     * @param image 업로드할 프로필 이미지 파일
     * @return 업데이트된 사용자 정보
     */
    @Operation(
            summary = "프로필 이미지 업데이트 (completed)",
            description = "사용자의 프로필 이미지를 업로드합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "프로필 이미지 업로드 성공",
            content = @Content(schema = @Schema(implementation = UserDetailResponseDto.class))
    )
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED_ACCESS, ErrorCode.INVALID_INPUT_VALUE, ErrorCode.FILE_UPLOAD_ERROR})
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

    /** 유저가 작성한 한줄 리뷰 리스트 (최신 등록순) 조회 */
    @Operation(
            summary = "내가 작성한 한줄 리뷰 리스트 조회 (completed)",
            description = "인증된 사용자가 작성한 한줄 리뷰 목록을 최신순으로 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "리뷰 리스트 조회 성공",
            content = @Content(schema = @Schema(implementation = UserReviewListResponse.class)))
    @ApiErrorResponses({ErrorCode.STORE_REVIEW_SERVICE_ERROR, ErrorCode.STORE_NOT_FOUND})
    @GetMapping("/me/reviews/short")
    public ResponseEntity<UserReviewListResponse> getMyShortReviews() {
        UserReviewListResponse response = storeReviewService.getUserReviewList();
        return ResponseEntity.ok(response);
    }
}