package org.swyp.dessertbee.common.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
     * HTTP 메시지 파싱 예외 처리
     * 요청 본문을 객체로 변환할 수 없을 때 발생하는 예외 처리
     * ENUM값 처리를 위해서 일단 만들었는데 다른 파싱 예외도 추가하시면 됩니다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("요청 본문 파싱 실패: {}", e.getMessage());

        // 열거형 값 오류인지 확인
        if (e.getCause() instanceof InvalidFormatException) {
            InvalidFormatException cause = (InvalidFormatException) e.getCause();
            if (cause.getTargetType() != null && cause.getTargetType().isEnum()) {
                // 로그에는 상세 정보 기록
                log.warn("유효하지 않은 열거형 값: {}, 타입: {}", cause.getValue(), cause.getTargetType().getSimpleName());
                return ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 입력값입니다.");
            }
        }

        return ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, "잘못된 요청 형식입니다.");
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