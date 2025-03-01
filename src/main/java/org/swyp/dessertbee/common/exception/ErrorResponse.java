package org.swyp.dessertbee.common.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

/**
 * 에러 응답을 위한 DTO 클래스
 * 클라이언트에게 일관된 형식의 에러 정보를 제공
 */
@Getter
@Builder
public class ErrorResponse {
    private final int status;          // HTTP 상태 코드
    private final String code;          // 에러 코드
    private final String message;       // 에러 메시지
    private final LocalDateTime timestamp;  // 에러 발생 시간

    /**
     * ErrorCode로부터 ErrorResponse 생성
     */
    public static ResponseEntity<ErrorResponse> of(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ErrorResponse.builder()
                                .status(errorCode.getHttpStatus().value())
                                .code(errorCode.getCode())
                                .message(errorCode.getMessage())
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    /**
     * ErrorCode와 커스텀 메시지로 ErrorResponse 생성
     */
    public static ResponseEntity<ErrorResponse> of(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ErrorResponse.builder()
                                .status(errorCode.getHttpStatus().value())
                                .code(errorCode.getCode())
                                .message(message)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    /**
     * BusinessException으로부터 ErrorResponse 생성
     */
    public static ResponseEntity<ErrorResponse> of(BusinessException ex) {
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(
                        ErrorResponse.builder()
                                .status(ex.getErrorCode().getHttpStatus().value())
                                .code(ex.getErrorCode().getCode())
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    public static ErrorResponse from(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}