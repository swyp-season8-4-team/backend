package org.swyp.dessertbee.email.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.email.dto.EmailVerificationRequestDto;
import org.swyp.dessertbee.email.dto.EmailVerificationResponseDto;
import org.swyp.dessertbee.email.dto.EmailVerifyRequestDto;
import org.swyp.dessertbee.email.dto.EmailVerifyResponseDto;
import org.swyp.dessertbee.email.service.EmailVerificationService;
import org.swyp.dessertbee.email.service.EmailVerificationServiceImpl;

import java.util.Collections;
import java.util.Map;

/**
 * 이메일 인증 관련 API Controller
 */
@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * 이메일 인증 코드 발송 API
     */
    @PostMapping("/verification-request")
    public ResponseEntity<EmailVerificationResponseDto> requestVerification(
            @Valid @RequestBody EmailVerificationRequestDto request
    ) {
        EmailVerificationResponseDto response =
                emailVerificationService.sendVerificationEmail(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 인증 코드 확인 API
     */
    @PostMapping("/verify")
    public ResponseEntity<EmailVerifyResponseDto> verifyEmail(
            @Valid @RequestBody EmailVerifyRequestDto request
    ) {
        EmailVerifyResponseDto response =
                emailVerificationService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 인증 관련 예외 처리
     */
    @ExceptionHandler(EmailVerificationServiceImpl.InvalidVerificationException.class)
    public ResponseEntity<Map<String, String>> handleInvalidVerification(EmailVerificationServiceImpl.InvalidVerificationException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("message", e.getMessage()));
    }
}