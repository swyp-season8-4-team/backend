package org.swyp.dessertbee.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리를 위한 핸들러
 * 애플리케이션에서 발생하는 모든 예외를 일관된 형식으로 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     * 비즈니스 로직에서 발생하는 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        return ErrorResponse.of(e);
    }

    /**
     * 입력값 검증 실패 처리
     * @Valid 검증 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE,
                e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * 그 외 모든 예외 처리
     * 예상치 못한 예외를 처리
     * 미처 처리하지 못한 모든 예외를 잡아서 INTERNAL_SERVER_ERROR(500)로 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception: ", e);
        return ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}