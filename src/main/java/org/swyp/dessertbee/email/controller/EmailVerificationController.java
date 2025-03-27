package org.swyp.dessertbee.email.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.email.dto.EmailVerificationRequestDto;
import org.swyp.dessertbee.email.dto.EmailVerificationResponseDto;
import org.swyp.dessertbee.email.dto.EmailVerifyRequestDto;
import org.swyp.dessertbee.email.dto.EmailVerifyResponseDto;
import org.swyp.dessertbee.email.service.EmailVerificationService;

/**
 * 이메일 인증 관련 API Controller
 */
@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Verification", description = "이메일 인증 관련 API")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * 이메일 인증 코드 발송 API
     */
    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입, 비밀번호 재설정 등에 필요한 인증 코드를 이메일로 발송합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "인증 코드 발송 성공",
            content = @Content(schema = @Schema(implementation = EmailVerificationResponseDto.class))
    )
    @ApiErrorResponses({
            ErrorCode.DUPLICATE_EMAIL,
            ErrorCode.TOO_MANY_VERIFICATION_REQUESTS,
            ErrorCode.SIGNUP_RESTRICTED_DELETED_ACCOUNT,
            ErrorCode.EMAIL_SENDING_FAILED,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/verification-request")
    public ResponseEntity<EmailVerificationResponseDto> requestVerification(
            @Parameter(description = "이메일 인증 요청 정보", required = true)
            @Valid @RequestBody EmailVerificationRequestDto request
    ) {
        EmailVerificationResponseDto response =
                emailVerificationService.sendVerificationEmail(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 인증 코드 확인 API
     */
    @Operation(summary = "이메일 인증 코드 확인", description = "사용자가 입력한 인증 코드의 유효성을 검증합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "인증 코드 확인 성공",
            content = @Content(schema = @Schema(implementation = EmailVerifyResponseDto.class))
    )
    @ApiErrorResponses({
            ErrorCode.INVALID_VERIFICATION_TOKEN,
            ErrorCode.EXPIRED_VERIFICATION_TOKEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/verify")
    public ResponseEntity<EmailVerifyResponseDto> verifyEmail(
            @Parameter(description = "이메일 인증 코드 확인 요청 정보", required = true)
            @Valid @RequestBody EmailVerifyRequestDto request
    ) {
        EmailVerifyResponseDto response =
                emailVerificationService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }
}