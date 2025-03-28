package org.swyp.dessertbee.user.exception;

import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

/**
 * 유저 관련 예외 클래스들을 모아둔 클래스
 */
public class UserExceptions {

    /**
     * 유효하지 않은 사용자 상태 예외
     */
    public static class InvalidUserStatusException extends BusinessException {
        public InvalidUserStatusException() {
            super(ErrorCode.INVALID_USER_STATUS);
        }

        public InvalidUserStatusException(String message) {
            super(ErrorCode.INVALID_USER_STATUS, message);
        }
    }

    /**
     * 존재하지 않는 사용자 예외
     */
    public static class UserNotFoundException extends BusinessException {
        public UserNotFoundException() {
            super(ErrorCode.USER_NOT_FOUND);
        }

        public UserNotFoundException(String message) {
            super(ErrorCode.USER_NOT_FOUND, message);
        }
    }

    /**
     * 권한이 없는 사용자 예외
     */
    public static class UnauthorizedAccessException extends BusinessException {
        public UnauthorizedAccessException() {
            super(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        public UnauthorizedAccessException(String message) {
            super(ErrorCode.UNAUTHORIZED_ACCESS, message);
        }
    }

    /**
     * 유효하지 않은 사용자 uuid 예외
     */
    public static class InvalidUserUuidException extends BusinessException {
        public InvalidUserUuidException() {
            super(ErrorCode.INVALID_USER_UUID);
        }

        public InvalidUserUuidException(String message) {
            super(ErrorCode.INVALID_USER_UUID, message);
        }
    }
}
